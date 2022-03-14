import {model, moment, ng, template, toasts} from 'entcore';
import {IWorld, Worlds} from "../models";
import {minetestService} from "../services";
import {DateUtils} from "../utils/date.utils";
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

        this.lightbox = {
            create: false,
            sharing: false,
            invitation: false,
            delete: false,
        };

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

    closeCreationLightbox(): void {
        template.close('lightbox');
        this.lightbox.create = false;
    }

    closeDeleteLightbox(): void {
        template.close('lightbox');
        this.lightbox.delete = false;
    }

    async createWorld(): Promise<void> {
        this.world = {
            owner_id: this.user_id,
            owner_name: this.user_name,
            created_at: DateUtils.format(moment().startOf('minute'), "DD/MM/YYYY HH:mm"),
            updated_at: DateUtils.format(moment().startOf('minute'), "DD/MM/YYYY HH:mm"),
            password: this.world.password,
            status: false,
            title: this.world.title,
            selected: false
        }

        let response = await minetestService.create(this.world);
        if (response) {
            toasts.confirm('minetest.world.create.confirm');
            this.closeCreationLightbox();
            await this.getWorld();
            this.$scope.$apply();
        } else {
            toasts.warning('minetest.world.create.error');
        }
    }

    createWorldLightbox(): void {
        template.open('lightbox', 'world-creation');
        this.lightbox.create = true;
    }

    async deleteWorld(): Promise<void> {
        let selectedWorldIs = Array<String>();
        this.selectedWorld.forEach(world => {
            if (world.selected) selectedWorldIs.push(world._id);
        })
        let response = await minetestService.delete(selectedWorldIs);
        if (response) {
            toasts.confirm('minetest.world.delete.confirm');
            this.closeDeleteLightbox();
            template.close('section');
            await this.getWorld();
            this.$scope.$apply();
        } else {
            toasts.warning('minetest.world.delete.error');
        }
    }

    async deleteWorldLightbox(): Promise<void> {
        let selectedWorlds = new Array<IWorld>();
        this.worlds.all.forEach(world => {
            if (world.selected) selectedWorlds.push(world)
        });
        this.selectedWorld = selectedWorlds;
        template.open('lightbox', 'world-delete');
        this.lightbox.delete = true;
    }

    getNbSelectedWorld(): number {
        let selectedWorld: number = 0;
        this.worlds.all.forEach((world: IWorld) => {
            if (world.selected) selectedWorld++;
        });
        return selectedWorld;
    }

    async getWorld(): Promise<void> {
        this.worlds.all = await minetestService.get(this.user_id, this.user_name);
    }

    oneWorldSelected(): boolean {
        return this.getNbSelectedWorld() == 1;
    }

    async setStatusWorld(currentWorld: IWorld): Promise<void> {
        this.currentWorld.status = !currentWorld.status;
    }

    toggleWorld(world): void {
        world.selected = !world.selected;
        this.selected = !this.selected;
        template.open('toggle', 'toggle-bar');
        if(this.oneWorldSelected()) {
            this.currentWorld = world;
            template.open('section', 'current-world');
        }
        else {
            template.close('section');
        }
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

}

export const minetestController = ng.controller('MinetestController', ['$scope', Controller]);
