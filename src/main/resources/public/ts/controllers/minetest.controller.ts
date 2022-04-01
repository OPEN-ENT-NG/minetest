import {model, moment, ng, toasts, idiom as lang} from 'entcore';
import {IWorld, Worlds} from "../models";
import {minetestService} from "../services";
import {IScope} from "angular";

class Controller implements ng.IController {
    currentWorld: IWorld;
    display: { allowPassword: boolean };
    filter: { creation_date: Date; up_date: Date; guests: any; shared: boolean; title: string };
    selected: boolean;
    selectedWorld: Array<IWorld>;
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
        this.selected = false;
        this.currentWorld = {} as IWorld;
        this.selectedWorld = [];

        this.filter = {
            creation_date: null,
            up_date: null,
            guests: [],
            shared: false,
            title: ""
        };

        this.display = { allowPassword: false };

        this.worlds = new Worlds();
        this.worlds.all = [];
        this.initData();
    }

    $onDestroy(): any {
    }

    async initData(): Promise<void> {
        await this.getWorld();
        this.$scope.$apply();
    }

    async getWorld(): Promise<void> {
        this.worlds.all = await minetestService.get(this.user_id, this.user_name);
        this.setCurrentWorld();
    }

    setCurrentWorld(): void {
        this.currentWorld = this.worlds.all[0];
    }

    setStatus(world): void {
        let open = lang.translate('minetest.open');
        let close = lang.translate('minetest.close');
        if(world.status) {
            return open;
        } else return close;
    }

    async updateWorld(): Promise<void> {
        let world = {
            id: this.world._id,
            owner_id: this.world.owner_id,
            owner_name: this.world.owner_name,
            owner_login: this.world.owner_login,
            created_at: this.world.created_at,
            updated_at: moment().startOf('day')._d,
            password: this.world.password,
            status: false,
            title: this.world.title,
            selected: false,
            img: this.world.img,
            shared: this.world.shared,
            address: this.world.address,
        }
        let response = await minetestService.update(world);
        if (response) {
            toasts.confirm('minetest.world.create.confirm');
        } else {
            toasts.warning('minetest.world.create.error');
        }
    }

    // TODO IS USED TO CALLBACK
    async refreshWorldList() {
        await this.getWorld();
        this.$scope.$apply();
    }

}

export const minetestController = ng.controller('MinetestController', ['$scope', Controller]);
