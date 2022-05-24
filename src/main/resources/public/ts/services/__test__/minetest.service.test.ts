import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import {minetestService} from '../minetest.service';
import {IImportWorld, IWorld} from "../../models";

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

    // import world

    it('checks create response is correct', done => {
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

        minetestService.import(importWorld).then(response => {
            expect(response.status).toEqual(200);
            expect(response.config.url).toEqual(`/minetest/worlds/import`);
            expect(response.config.method).toEqual(`post`);
            expect(JSON.parse(response.config.data)).toEqual(importWorld);
            done();
        });
    });

    it('checks update response is correct', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};

        const world: IWorld = {
                    owner_id: "111",
                    owner_name: "name",
                    owner_login: "111",

                    created_at: "created_at",
                    updated_at: "updated_at",

                    password: "password",
                    title: "monde1",
                    address: "world.fr",
                    port: 30000

                }

        mock.onPut(`/minetest/worlds/import/${world._id}`, world)
            .reply(200, data);

        minetestService.updateImportWorld(world).then(response => {
            expect(response.status).toEqual(200);
            expect(response.config.url).toEqual(`/minetest/worlds/import/${world._id}`);
            expect(response.config.method).toEqual(`put`);
            expect(JSON.parse(response.config.data)).toEqual(world);
            done();
        });
    });

    it('checks delete response is correct', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};
        const world: IWorld = {
            owner_id: "111",
            owner_name: "name",
            owner_login: "111",

            created_at: "created_at",
            updated_at: "updated_at",

            password: "password",
            title: "monde1",
            address: "world.fr",
            port: 30000

        }

        mock.onDelete(`/minetest/worlds/import?id=${world._id}`, world)
            .reply(200, data);

        minetestService.deleteImportWorld(world).then(response => {
            expect(response.status).toEqual(200);
            expect(response.config.url).toEqual(`/minetest/worlds/import?id=${world._id}`);
            expect(response.config.method).toEqual(`delete`);
            done();
        });
    });
});
