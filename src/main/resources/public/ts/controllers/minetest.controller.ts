import {model, ng, idiom as lang} from 'entcore';
import {IWorld, Worlds} from "../models";
import {minetestService} from "../services";
import {IScope} from "angular";

declare let window: any;

interface IViewModel {
    initData(): Promise<void>;
    getWorld(): Promise<void>;
    setCurrentWorld(): void;
    setStatus(world: IWorld): string;
    getLink(): string;
    refreshWorldList(): Promise<void>;

    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    currentWorld: IWorld;
    display: { allowPassword: boolean };
    filter: { creation_date: Date; up_date: Date; guests: any; shared: boolean; title: string };
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

    setStatus(world: IWorld): string {
        let open: string = lang.translate('minetest.open');
        let close: string = lang.translate('minetest.close');
        if(world.status) {
            return open;
        } else return close;
    }

    getLink(): string {
        return window.minetestDownload;
    }

    async refreshWorldList(): Promise<void> {
        await this.getWorld();
        this.$scope.$apply();
    }
}

export const minetestController = ng.controller('MinetestController', ['$scope', Controller]);
