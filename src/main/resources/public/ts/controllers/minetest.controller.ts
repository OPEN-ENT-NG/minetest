import {model, ng, idiom as lang, _, Rights, Behaviours} from 'entcore';
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

    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    currentWorld: IWorld;
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
        this.display.loading = false;
    }

    setCurrentWorld(world?: IWorld): void {
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

    refreshWorldList(): (world?: IWorld) => void {
        let self: Controller = this;
        return (world: any): void => {
            self.getWorld(world);
        }
    }

    filterWorlds(searching:string): IWorld[] {
        if(searching){
            return _.filter(this.worlds.all, (world) =>
                world.owner_name.toLowerCase().includes(searching.toLowerCase()) ||
                    world.title.toLowerCase().includes(searching.toLowerCase())
            );
        } else {
            return this.worlds.all;
        }
    }
}

export const minetestController = ng.controller('MinetestController', ['$scope', Controller]);
