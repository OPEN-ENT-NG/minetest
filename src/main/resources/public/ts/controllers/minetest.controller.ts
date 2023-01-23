import {model, ng, idiom as lang, _, Behaviours} from 'entcore';
import {IWorld, Worlds} from "../models";
import {minetestService} from "../services";
import {IScope} from "angular";
import {safeApply} from "../utils/safe-apply.utils";

declare let window: any;

interface IViewModel {
    initData(): Promise<void>;
    getWorld(): Promise<void>;
    setCurrentWorld(world?: IWorld): void;
    setStatus(world: IWorld): string;
    getLink(): string;
    getWiki(): string;
    getDownload(): string;
    refreshWorldList(world?: IWorld): void;
    filterWorlds(searching: string): IWorld[];
    clickOnWorld(world: IWorld): void;
    worldIsSelected(world: IWorld): boolean;
    hasManagerRightOnSelectedWorlds(): boolean;

    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    currentWorld: IWorld;
    selectedWorlds: Worlds;
    display: { allowPassword: boolean, loading: boolean };
    filter: { creation_date: Date; up_date: Date; guests: any; shared: boolean; title: string };
    user_id: string;
    user_name: string;
    user_login: string;
    world: IWorld;
    worlds: Worlds;

    constructor(private $scope: IScope) {
        this.$scope['vm'] = this;
    }

    $onInit(): any {
        this.user_id = model.me.userId;
        this.user_name = model.me.username;
        this.user_login = model.me.login;

        this.filter = {
            creation_date: null,
            up_date: null,
            guests: [],
            shared: false,
            title: ""
        };

        this.display = { allowPassword: false, loading: true };
        this.worlds = new Worlds();
        this.worlds.all = [];
        this.selectedWorlds = new Worlds();
        this.selectedWorlds.all = [];
        this.initData();
    }

    $onDestroy(): any {
    }

    async initData(): Promise<void> {
        await this.getWorld();
        safeApply(this.$scope);
    }

    async getWorld(world?: IWorld): Promise<void> {
        this.display.loading = true;
        this.worlds.all = await minetestService.get(this.user_id, this.user_name);
        this.worlds.all.forEach( (world: IWorld) => {
            if (!_.isEmpty(world)) {
                world.owner = {
                    userId: world.owner_id,
                    displayName: world.owner_name
                };
                Behaviours.applicationsBehaviours.minetest.resource(world);
            }
        });
        if (world) {
            let currentWorld: IWorld = this.worlds.all.find(w => w._id === world._id)
            currentWorld ? this.setCurrentWorld(currentWorld) : this.setCurrentWorld();
        } else {
            this.setCurrentWorld();
        }
        if (this.selectedWorlds.all.length === 0) {
            this.selectedWorlds.all.push(this.currentWorld);
        }
        this.display.loading = false;
        safeApply(this.$scope);
    }

    setCurrentWorld(world?: IWorld): void {
        this.currentWorld = world || this.currentWorld;
        if (this.currentWorld) this.currentWorld = this.filterWorlds(this.filter.title).find(w => w._id === this.currentWorld._id);
        this.currentWorld = this.currentWorld || this.filterWorlds(this.filter.title)[0];
        safeApply(this.$scope);
    }

    setStatus(world: IWorld): string {
        let open: string = lang.translate('minetest.open');
        let close: string = lang.translate('minetest.close');
        let created: string = lang.translate('minetest.created');

        if (world.created_at == world.updated_at) {
            return created;
        }
        
        // check if world has defined password and check its status to set status type
        if (world.password) {
            return world.status ? open : close;
        }
        
        return created;
    }

    getLink(): string {
        return window.minetestLink;
    }

    getWiki(): string {
        return window.minetestWiki;
    }

    getDownload(): string {
        return window.minetestDownload;
    }

    refreshWorldList(): (world?: IWorld) => void {
        let self: Controller = this;
        return (world: any): void => {
            self.getWorld(world);
        }
    }

    filterWorlds(searching: string): IWorld[] {
        if(searching){
            return _.filter(this.worlds.all, (world) =>
                world.owner_name.toLowerCase().includes(searching.toLowerCase()) ||
                    world.title.toLowerCase().includes(searching.toLowerCase())
            );
        } else {
            return this.worlds.all;
        }
    }

    clickOnWorld(world: IWorld): void {
        if (this.selectedWorlds.all.length === 0) {
            this.selectedWorlds.all.push(world);
            this.setCurrentWorld(world);
        } else {
            if (this.selectedWorlds.all.find(w => w._id === world._id) && this.selectedWorlds.all.length > 1) {
                this.selectedWorlds.all = this.selectedWorlds.all.filter(w => w._id != world._id);
                if (this.selectedWorlds.all.length === 1) {
                    this.setCurrentWorld(this.selectedWorlds.all[0]);
                }
            } else if (!this.selectedWorlds.all.find(w => w._id === world._id)) {
                this.selectedWorlds.all.push(world);
                this.currentWorld = undefined;
            }
        }
    }

    worldIsSelected(world: IWorld): boolean {
        return this.selectedWorlds.all.find(w => w._id === world._id) !== undefined;
    }

    hasManagerRightOnSelectedWorlds(): boolean {
        return this.selectedWorlds.all.filter(w => w.myRights.manager).length == this.selectedWorlds.all.length;
    }
}

export const minetestController = ng.controller('MinetestController', ['$scope', Controller]);
