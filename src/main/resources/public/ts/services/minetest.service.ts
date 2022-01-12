import {ng} from 'entcore'
import http, {AxiosResponse} from 'axios';

export interface IMinetestService {
    test(): Promise<AxiosResponse>;
}

export const minetestService: IMinetestService = {
    test: async (): Promise<AxiosResponse> => {
        return http.get(`/minetest/test/ok`);
    }
};

export const MinetestService = ng.service('MinetestService', (): IMinetestService => minetestService);