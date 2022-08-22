import {Behaviours, model} from 'entcore';

console.log('minetest behaviours loaded');

const minetestBehaviours = {
    resources: {
        contrib: {
            right: "fr-openent-minetest-controller-ShareWorldController|initContribResourceRight"
        },
        manager: {
            right: "fr-openent-minetest-controller-ShareWorldController|initManagerResourceRight"
        }
    }
};

Behaviours.register('minetest',{
    rights: minetestBehaviours,
    dependencies: {},
    /**
     * Allows to set rights for behaviours.
     */
    resource : function(resource) {
        let rightsContainer = resource;
        if (resource && !resource.myRights) {
            resource.myRights = {};
        }
        for (const behaviour in minetestBehaviours.resources) {
            if (model.me.hasRight(rightsContainer, minetestBehaviours.resources[behaviour]) ||
                model.me.userId === resource.owner.userId || model.me.userId === rightsContainer.owner.userId) {
                if (resource.myRights[behaviour] !== undefined) {
                    resource.myRights[behaviour] = resource.myRights[behaviour] && minetestBehaviours.resources[behaviour];
                } else {
                    resource.myRights[behaviour] = minetestBehaviours.resources[behaviour];
                }
            }
        }
        return resource;
    },

    /**
     * Allows to define all rights to display in the share windows. Names are
     * defined in the server part with
     * <code>@SecuredAction(value = "xxxx.read", type = ActionType.RESOURCE)</code>
     * without the prefix <code>xxx</code>.
     */
    resourceRights: function () {
        return ['contrib', 'manager'];
    },
});
