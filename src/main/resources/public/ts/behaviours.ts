import {Behaviours, model} from 'entcore';

const rights = {
    resources: {
        contrib: {
            right: "fr-openent-minetest-controller-MinetestController|initContribResourceRight"
        },
        manager: {
            right: "fr-openent-minetest-controller-MinetestController|initManagerResourceRight"
        }
    }
};

Behaviours.register('minetest',{
    rights: rights,
    dependencies: {},
    resourceRights: function () {
        return ['contrib', 'manager'];
    }
});
