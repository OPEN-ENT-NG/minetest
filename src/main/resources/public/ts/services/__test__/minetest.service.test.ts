import {http} from 'entcore-toolkit';
import {minetestService} from '../minetest.service';
import {IImportWorld, IWorld} from "../../models";
import {mockHttpResponse} from "../../../../../../../test-utils/httpMock";

jest.mock('entcore-toolkit', () => ({
    ...jest.requireActual('entcore-toolkit'),
    http: {get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), postFile: jest.fn(), putFile: jest.fn()}
}));

describe('MinetestService', () => {
    it('returns data when retrieve request is correctly called', done => {
        const data = {response: true};
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        minetestService.test().then(response => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    // import world

    it('checks create response is correct', done => {
        const data = {response: true};

        const importWorld: IImportWorld = {
            myRights: {},
            owner: {displayName: "name", userId: "111"},
            shared: [],
            owner_id: "111",
            owner_name: "name",
            owner_login: "111",

            created_at: "created_at",
            updated_at: "updated_at",

            title: "monde1",
            address: "world.fr",
            port: 30000,

            isExternal: true
        };

        (http.post as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data, {
            url: `/minetest/worlds/import`,
            method: 'post',
            data: JSON.stringify(importWorld)
        }));

        minetestService.import(importWorld).then(response => {
            expect(response.status).toEqual(200);
            expect((response.config as any).url).toEqual(`/minetest/worlds/import`);
            expect((response.config as any).method).toEqual(`post`);
            expect(JSON.parse((response.config as any).data)).toEqual(importWorld);
            done();
        });
    });

    it('checks update response is correct', done => {
        const data = {response: true};

        const world: IWorld = {
            myRights: {},
            owner: {displayName: "name", userId: "111"},
            shared: [],
            owner_id: "111",
            owner_name: "name",
            owner_login: "111",

            created_at: "created_at",
            updated_at: "updated_at",

            password: "password",
            title: "monde1",
            address: "world.fr",
            port: 30000,
            shuttingDown: true

        };

        (http.put as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data, {
            url: `/minetest/worlds/import/${world._id}`,
            method: 'put',
            data: JSON.stringify(world)
        }));

        minetestService.updateImportWorld(world).then(response => {
            expect(response.status).toEqual(200);
            expect((response.config as any).url).toEqual(`/minetest/worlds/import/${world._id}`);
            expect((response.config as any).method).toEqual(`put`);
            expect(JSON.parse((response.config as any).data)).toEqual(world);
            done();
        });
    });

    it('checks delete response is correct', done => {
        const data = {response: true};
        const world: IWorld = {
            myRights: {},
            owner: {displayName: "name", userId: "111"},
            shared: [],
            owner_id: "111",
            owner_name: "name",
            owner_login: "111",

            created_at: "created_at",
            updated_at: "updated_at",

            password: "password",
            title: "monde1",
            address: "world.fr",
            port: 30000,
            shuttingDown: true

        };

        (http.delete as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data, {
            url: `/minetest/worlds/import/${world._id}`,
            method: 'delete'
        }));

        minetestService.deleteImportWorld(world).then(response => {
            expect(response.status).toEqual(200);
            expect((response.config as any).url).toEqual(`/minetest/worlds/import/${world._id}`);
            expect((response.config as any).method).toEqual(`delete`);
            done();
        });
    });
});
