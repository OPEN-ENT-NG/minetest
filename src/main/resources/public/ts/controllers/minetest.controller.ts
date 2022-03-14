import {model, moment, ng, template, toasts} from 'entcore';
import {IWorld, Worlds} from "../models";
import {minetestService} from "../services";
import {DateUtils} from "../utils/date.utils";

interface ViewModel {
    $onInit(): any;
    $onDestroy(): any;

    user_id: string;
    user_name: string;
    getMomentFromDate: any;

    lightbox: {
        create: boolean,
        sharing: boolean,
        invitation: boolean,
        delete: boolean,
    }

    initData(): Promise<void>;

    downloadMinetest(): void;

    getWorld(): Promise<void>;

    // component
    createWorld(): Promise<void>;
    updateWorld(): Promise<void>;
    deleteWorld(selectedWorld: Array<IWorld>): Promise<void>;
    setStatusWorld(selectedWorld: IWorld): Promise<void>;

    toggleWorld(world): void;

    createWorldLightbox(): void;
    closeCreationLightbox(): void;

    deleteWorldLightbox(): void;
    closeDeleteLightbox(): void;

    getNbSelectedWorld(): number;
    oneWorldSelected(): boolean;

    selected: boolean;
    display: { allowPassword: boolean };

    world: IWorld;
    worlds: Worlds;

    selectedWorld: Array<IWorld>;
    currentWorld: IWorld;
    text: string;

    // Filter
    filter: { creation_date: Date, up_date: Date, guests: any, shared: boolean, title: string };
}

// we use function instead of arrow function to apply life's cycle hook
export const minetestController = ng.controller('MinetestController', ['$scope', 'route', function ($scope, route) {
    const vm: ViewModel = this;



    // init life's cycle hook
    // vm.$onInit = () => {
    //     vm.text = "test var";
    //     console.log("vm parent (main): ", $scope.$parent.vm);
    // };
    //
    // // destruction cycle hook
    // vm.$onDestroy = () => {
    //
    // };


    const initData = async (): Promise<void> => {

    };

    // init life's cycle hook
    vm.$onInit = () => {
        vm.user_id = model.me.userId;
        vm.user_name = model.me.username;
    };

    vm.selected = false;
    vm.currentWorld = {} as IWorld;
    vm.selectedWorld = [];

    vm.lightbox = {
        create: false,
        sharing: false,
        invitation: false,
        delete: false,
    };

    vm.filter = {
        creation_date: null,
        up_date: null,
        guests: [],
        shared: false,
        title: ""
    };

    vm.display = { allowPassword: false };

    vm.worlds = new Worlds();
    vm.worlds.all = [];

    vm.getWorld = async(): Promise<void> => {
        vm.worlds.all = await minetestService.get(vm.user_id, vm.user_name);
    };

    vm.getNbSelectedWorld = (): number => {
        let selectedWorld: number = 0;
        vm.worlds.all.forEach((world: IWorld) => {
            if (world.selected) selectedWorld++;
        });
        return selectedWorld;
    };

    vm.createWorldLightbox = (): void => {
        template.open('lightbox', 'world-creation');
        vm.lightbox.create = true;
    };

    vm.closeCreationLightbox = (): void => {
        template.close('lightbox');
        vm.lightbox.create = false;
    };

    vm.deleteWorldLightbox = (): void => {
        let selectedWorld = new Array<IWorld>();
        vm.worlds.all.forEach(world => {
            if (world.selected) selectedWorld.push(world)
        });
        vm.selectedWorld = selectedWorld;
        template.open('lightbox', 'world-delete');
        vm.lightbox.delete = true;
    }

    vm.closeDeleteLightbox = (): void => {
        template.close('lightbox');
        vm.lightbox.delete = false;
    };

    vm.createWorld = async():Promise<void> => {
        console.log('i\'\ m create here');
        vm.world = {
            owner_id: vm.user_id,
            owner_name: vm.user_name,
            created_at: DateUtils.format(moment().startOf('minute'), "DD/MM/YYYY HH:mm"),
            updated_at: DateUtils.format(moment().startOf('minute'), "DD/MM/YYYY HH:mm"),
            password: vm.world.password,
            status: false,
            title: vm.world.title,
            selected: false
        }

        let response = await minetestService.create(vm.world);
        if (response) {
            toasts.confirm('minetest.world.create.confirm');
            vm.closeCreationLightbox();
            await vm.getWorld();
            $scope.$apply();
        } else {
            toasts.warning('minetest.world.create.error');
        }
    };

    vm.updateWorld = async():Promise<void> => {
        let world = {
            id: vm.world._id,
            owner_id: vm.world.owner_id,
            owner_name: vm.world.owner_name,
            created_at: vm.world.created_at,
            updated_at: moment().startOf('day')._d,
            password: vm.world.password,
            status: false,
            title: vm.world.title,
            selected: false,
            img: vm.world.img,
            shared: vm.world.shared
        }
        let response = await minetestService.update(world);
        if (response) {
            toasts.confirm('minetest.world.create.confirm');
        } else {
            toasts.warning('minetest.world.create.error');
        }
    };

    vm.deleteWorld = async():Promise<void> => {
        let selectedWorldIs = Array<String>();
        vm.selectedWorld.forEach(world => {
            if (world.selected) selectedWorldIs.push(world._id);
        })
        let response = await minetestService.delete(selectedWorldIs);
        if (response) {
            toasts.confirm('minetest.world.delete.confirm');
            vm.getWorld();
            $scope.$apply();
        } else {
            toasts.warning('minetest.world.delete.error');
        }
    };

    vm.setStatusWorld = async(currentWorld: IWorld):Promise<void> => {
        if(!currentWorld.status) {
            vm.currentWorld.status = true
        } else vm.currentWorld.status = false;
        // !currentWorld.status ? currentWorld.status : !currentWorld.status;
    };

    vm.toggleWorld = (world: IWorld) => {
        world.selected = !world.selected;
        vm.selected = !vm.selected;
        template.open('toggle', 'toggle-bar');
        if(vm.oneWorldSelected()) {
            vm.currentWorld = world;
            template.open('section', 'current-world');
        }
        else {
            template.close('section');
        }
    }

    vm.oneWorldSelected = () => {
        return vm.getNbSelectedWorld() == 1;
    }

    vm.getMomentFromDate = (date,time) => {
        return  moment([
            date.getFullYear(),
            date.getMonth(),
            date.getDate(),
            time.hour(),
            time.minute()
        ])
    };

    initData().then(() => {
        vm.getWorld();
    })
}]);
