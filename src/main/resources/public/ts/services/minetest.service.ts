import {ng} from 'entcore'
import http, {AxiosResponse} from 'axios';
import {IImportWorld, IWorld} from "../models";

declare let window: any;

export interface IMinetestService {
    test(): Promise<AxiosResponse>;

    get(userId: string, userName: string): Promise<IWorld[]>;

    // Settings
    create(worldBody: IWorld): Promise<AxiosResponse>;

    import(worldBody: IImportWorld): Promise<AxiosResponse>;

    update(worldBody: IWorld): Promise<AxiosResponse>;

    updateImportWorld(worldBody: IWorld): Promise<AxiosResponse>;

    updateStatus(worldBody: IWorld): Promise<AxiosResponse>;

    getSharebookmarks(): Promise<AxiosResponse>;

    getSharebookmark(id: string): Promise<AxiosResponse>;

    getVisibleUsers(search: string): Promise<AxiosResponse>;

    invite(worldBody: IWorld | IImportWorld): Promise<IWorld>;

    delete(world: IWorld): Promise<AxiosResponse>;

    deleteImportWorld(world: IWorld): Promise<AxiosResponse>;
}

export const minetestService: IMinetestService = {
    test: async (): Promise<AxiosResponse> => {
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
    create: (worldBody: IWorld): Promise<AxiosResponse> => {
        return http.post(`/minetest/worlds`, worldBody);
    },

    import: (worldBody: IImportWorld): Promise<AxiosResponse> => {
        return http.post(`/minetest/worlds/import`, worldBody);
    },

    update: (worldBody: IWorld): Promise<AxiosResponse> => {
        return http.put(`/minetest/worlds/${worldBody._id}`, worldBody);
    },

    updateImportWorld: (worldBody: IWorld): Promise<AxiosResponse> => {
        return http.put(`/minetest/worlds/import/${worldBody._id}`, worldBody);
    },

    updateStatus: (worldBody: IWorld): Promise<AxiosResponse> => {
        return http.put(`/minetest/worlds/status/${worldBody._id}`, worldBody);
    },

    getSharebookmarks: (): Promise<AxiosResponse> => {
        return http.get('/directory/sharebookmark/all');
    },

    getSharebookmark: (id: string): Promise<AxiosResponse> => {
        return http.get('/directory/sharebookmark/' + id);
    },

    getVisibleUsers: (search: string): Promise<AxiosResponse> => {
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

    delete: (world: IWorld): Promise<AxiosResponse> => {
        return http.delete(`/minetest/worlds?id=${world._id}&port=${world.port}`);
    },

    deleteImportWorld: (world: IWorld): Promise<AxiosResponse> => {
        return http.delete(`/minetest/worlds/import/${world._id}`)
    }
};

export const MinetestService = ng.service('MinetestService', (): IMinetestService => minetestService);