import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import {minetestService} from '../minetest.service';

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
});
