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
    /**
     * Allows to set rights for behaviours.
     */
    resource : function(resource) {
        let rightsContainer = resource;

        if (resource && !resource.myRights) {
            resource.myRights = {};
        }

        for (const behaviour in rights.resources) {
            if (model.me.hasRight(rightsContainer, rights.resources[behaviour]) ||
                model.me.userId === resource.owner.userId || model.me.userId === rightsContainer.owner.userId) {
                if (resource.myRights[behaviour] !== undefined) {
                    resource.myRights[behaviour] = resource.myRights[behaviour] && rights.resources[behaviour];
                } else {
                    resource.myRights[behaviour] = rights.resources[behaviour];
                }
            }
        }
        return resource;
    },
    resourceRights: function () {
        return ['contrib', 'manager'];
    }
});
