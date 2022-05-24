import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import {minetestService} from '../minetest.service';
import {IImportWorld} from "../../models";

describe('MinetestService', () => {
    it('returns data when retrieve request is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onGet(`/minetest/test/ok`).reply(200, data);
        minetestService.test().then(response => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('checks if the answers of the import world had isExternal attribute', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};

        const importWorld: IImportWorld = {
            owner_id: "111",
            owner_name: "name",
            owner_login: "111",

            created_at: "created_at",
            updated_at: "updated_at",

            title: "monde1",
            address: "world.fr",
            port: "30000",

            isExternal: true
        }

        mock.onPost(`/minetest/worlds/import`, importWorld)
            .reply(200, data);
        done();
    });
});
