import {model, ng, idiom as lang} from 'entcore';
import {IWorld, Worlds} from "../models";
import {minetestService} from "../services";
import {IScope} from "angular";
import {safeApply} from "../utils/safe-apply.utils";

declare let window: any;

interface IViewModel {
    initData(): Promise<void>;
    getWorld(): Promise<void>;
    setCurrentWorld(): void;
    setStatus(world: IWorld): string;
    getLink(): string;
    getWiki(): string;
    getDownload(): string;
    refreshWorldList(world?): any;

    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    currentWorld: IWorld;
    display: { allowPassword: boolean };
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

        this.display = { allowPassword: false };
        this.worlds = new Worlds();
        this.worlds.all = [];
        this.initData();
    }

    $onDestroy(): any {
    }

    async initData(): Promise<void> {
        await this.getWorld();
        safeApply(this.$scope);
    }

    async getWorld(world?): Promise<void> {
        this.worlds.all = await minetestService.get(this.user_id, this.user_name);
        if (world) {
            let currentWorld = this.worlds.all.find(w => w._id === world._id)
            currentWorld ? this.setCurrentWorld(currentWorld) : this.setCurrentWorld();
        } else this.setCurrentWorld();
    }

    setCurrentWorld(world?): void {
        this.currentWorld = world || this.currentWorld;
        if (this.currentWorld) this.currentWorld = this.worlds.all.find(w => w._id === this.currentWorld._id);
        this.currentWorld = this.currentWorld || this.worlds.all[0];
        safeApply(this.$scope);
    }

    setStatus(world: IWorld): string {
        let open: string = lang.translate('minetest.open');
        let close: string = lang.translate('minetest.close');
        let added: string = lang.translate('minetest.added');

        if (world.password) {
            if (world.status) {
                return open;
            } else return close;
        }
        else return added;
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

    refreshWorldList(): (world) => any {
        let self: Controller = this;
        return (world: any): void => {
            self.getWorld(world);
        }
    }
}

export const minetestController = ng.controller('MinetestController', ['$scope', Controller]);
