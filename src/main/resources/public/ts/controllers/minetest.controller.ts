import {model, moment, ng, template, toasts} from 'entcore';
import {IWorld, Worlds} from "../models";
import {minetestService} from "../services";
import {IScope} from "angular";

class Controller implements ng.IController {
    currentWorld: IWorld;
    display: { allowPassword: boolean };
    filter: { creation_date: Date; up_date: Date; guests: any; shared: boolean; title: string };
    lightbox: { create: boolean; sharing: boolean; invitation: boolean; delete: boolean };
    selected: boolean;
    selectedWorld: Array<IWorld>;
    text: string;
    user_id: string;
    user_name: string;
    world: IWorld;
    worlds: Worlds;

    constructor(private $scope: IScope) {
        this.$scope['vm'] = this;
    }

    $onInit(): any {
        this.user_id = model.me.userId;
        this.user_name = model.me.username;
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
    }

    async setStatusWorld(currentWorld: IWorld): Promise<void> {
        this.currentWorld.status = !currentWorld.status;
    }

    toggleWorld(world): void {
        world.selected = !world.selected;
        this.selected = !this.selected;
        // template.open('toggle', 'toggle-bar');
        // todo must change logic ?
        // if(this.oneWorldSelected()) {
        //     this.currentWorld = world;
        //     template.open('section', 'current-world');
        // }
        // else {
        //     template.close('section');
        // }
    }

    async updateWorld(): Promise<void> {
        let world = {
            id: this.world._id,
            owner_id: this.world.owner_id,
            owner_name: this.world.owner_name,
            created_at: this.world.created_at,
            updated_at: moment().startOf('day')._d,
            password: this.world.password,
            status: false,
            title: this.world.title,
            selected: false,
            img: this.world.img,
            shared: this.world.shared
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
