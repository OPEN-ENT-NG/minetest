import {ng} from 'entcore'
import { http, HttpResponse } from 'entcore-toolkit';
import {IImportWorld, IWorld} from "../models";

declare let window: any;

export interface IMinetestService {
    test(): Promise<HttpResponse>;

    get(userId: string, userName: string): Promise<IWorld[]>;

    // Settings
    create(worldBody: IWorld): Promise<HttpResponse>;

    import(worldBody: IImportWorld): Promise<HttpResponse>;

    update(worldBody: IWorld): Promise<HttpResponse>;

    updateImportWorld(worldBody: IWorld): Promise<HttpResponse>;

    updateStatus(worldBody: IWorld): Promise<HttpResponse>;

    getSharebookmarks(): Promise<HttpResponse>;

    getSharebookmark(id: string): Promise<HttpResponse>;

    getVisibleUsers(search: string): Promise<HttpResponse>;

    invite(worldBody: IWorld | IImportWorld): Promise<IWorld>;

    delete(world: IWorld): Promise<HttpResponse>;

    deleteImportWorld(world: IWorld): Promise<HttpResponse>;
}

export const minetestService: IMinetestService = {
    test: async (): Promise<HttpResponse> => {
        return http.get(`/minetest/test/ok`);
    },

    get: async (userId: string, userName: string): Promise<IWorld[]> => {
        try {
            const {data} = await http.get(`/minetest/worlds?owner_id=${userId}` + `&owner_name=${userName}`);
            return data;
        } catch (err) {
            throw err;
        }
    },

    // Settings
    create: (worldBody: IWorld): Promise<HttpResponse> => {
        return http.post(`/minetest/worlds`, worldBody);
    },

    import: (worldBody: IImportWorld): Promise<HttpResponse> => {
        return http.post(`/minetest/worlds/import`, worldBody);
    },

    update: (worldBody: IWorld): Promise<HttpResponse> => {
        return http.put(`/minetest/worlds/${worldBody._id}`, worldBody);
    },

    updateImportWorld: (worldBody: IWorld): Promise<HttpResponse> => {
        return http.put(`/minetest/worlds/import/${worldBody._id}`, worldBody);
    },

    updateStatus: (worldBody: IWorld): Promise<HttpResponse> => {
        return http.put(`/minetest/worlds/status/${worldBody._id}`, worldBody);
    },

    getSharebookmarks: (): Promise<HttpResponse> => {
        return http.get('/directory/sharebookmark/all');
    },

    getSharebookmark: (id: string): Promise<HttpResponse> => {
        return http.get('/directory/sharebookmark/' + id);
    },

    getVisibleUsers: (search: string): Promise<HttpResponse> => {
        return http.get(`/${window.minetestMessaging}/visible?search=${search}`);
    },

    invite: async (worldBody: IWorld): Promise<IWorld> => {
        try {
            const {data} = await http.put(`/minetest/world/join/${worldBody._id}`, worldBody);
            return data;
        } catch (err) {
            throw err;
        }
    },

    delete: (world: IWorld): Promise<HttpResponse> => {
        return http.delete(`/minetest/worlds?id=${world._id}&port=${world.port}`);
    },

    deleteImportWorld: (world: IWorld): Promise<HttpResponse> => {
        return http.delete(`/minetest/worlds/import/${world._id}`)
    }
};

export const MinetestService = ng.service('MinetestService', (): IMinetestService => minetestService);