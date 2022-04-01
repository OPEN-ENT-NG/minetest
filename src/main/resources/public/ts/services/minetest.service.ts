import {ng} from 'entcore'
import http, {AxiosResponse} from 'axios';
import {IWorld} from "../models";

export interface IMinetestService {
    test(): Promise<AxiosResponse>;

    get(userId: string, userName: string): Promise<IWorld[]>;

    // Settings
    create(worldBody: IWorld): Promise<AxiosResponse>;

    createWithAttachment(worldBody: IWorld): Promise<AxiosResponse>;

    update(worldBody: IWorld): Promise<AxiosResponse>;

    delete(world: IWorld): Promise<AxiosResponse>;
}

export const minetestService: IMinetestService = {
    test: async (): Promise<AxiosResponse> => {
        return http.get(`/minetest/test/ok`);
    },

    get: async (userId: string, userName: string): Promise<IWorld[]> => {
        try {
            const {data} =
            await http.get(`/minetest/worlds?owner_id=${userId}` + `&owner_name=${userName}`);
            return data;
        } catch (err) {
            throw err;
        }
    },

    // Settings
    create: (worldBody: IWorld): Promise<AxiosResponse> => {
        return http.post(`/minetest/worlds`, worldBody);
    },

    createWithAttachment: (worldBody: IWorld): Promise<AxiosResponse> => {
        return http.post(`/minetest/worlds/attachment`, worldBody);
    },

    update: (worldBody: IWorld): Promise<AxiosResponse> => {
        return http.put(`/minetest/worlds/status`, worldBody);
    },

    delete: (world: IWorld): Promise<AxiosResponse> => {

        //TODO keep logic for later (multidelete)
        // let idParams: string = '';
        // if (worlds.length > 0) {
        //     worlds.forEach((_id: string) => {
        //         idParams += `&id=${_id}`;
        //     });
        // }

        return http.delete(`/minetest/worlds?id=${world._id}&port=${world.port}`);
    }
};

export const MinetestService = ng.service('MinetestService', (): IMinetestService => minetestService);