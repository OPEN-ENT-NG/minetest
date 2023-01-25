import {idiom, model, ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld, Worlds, Whitelist} from "../../models";
import {minetestService} from "../../services";
import {safeApply} from "../../utils/safe-apply.utils";

interface IViewModel {
    openShareLightbox(): void;
    closeShareLightbox(): void;
    canEditShareItem(args: any): boolean;
    onShareFeed(data: any, resource: IWorld, actions: any[]): void;
    onSubmit(shared: any): Promise<void>;

    lightbox: any;

    // props
    worlds: Worlds;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    worlds: Worlds;

    constructor(private $scope: IScope) {
        this.lightbox = {
            share: false,
        };
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openShareLightbox(): void {
        this.lightbox.share = true;
    }

    closeShareLightbox(): void {
        this.lightbox.share = false;
    }

    //code duplicate from workspace
    canEditShareItem(args: any) : boolean {
        if (args.type == "user") {
            const uniqOwnerIds : string[] = this.worlds.all.map((sha: IWorld) => sha.owner.userId)
                .filter((elem : string, pos: number, arr: string[]) => {
                return arr.indexOf(elem) == pos;
            });
            //can edit only if the user is not the owner
            if (uniqOwnerIds.length == 1) {
                return uniqOwnerIds[0] != args.id;
            }
        }
        return true;
    }

    //code duplicate from workspace
    onShareFeed(data: any, resource: IWorld, actions: any[]) : void {
        const userId : string = resource.owner.userId;
        //if owner is current user => skip
        if (userId == model.me.userId) {
            return;
        }
        //if owner is in shared or inherithshared=>skip
        if (data.users["checked"][userId] || (data.users["checkedInherited"] && data.users["checkedInherited"][userId])) {
            return;
        }
        //get ownername
        const userName : string = resource.owner.displayName;
        //add owner as manager => collect all actions
        const actionsNames : string[] = actions.map(a => a.name).reduce((prev, current) => prev.concat(current), [])
        if (!actionsNames.length) {
            throw "could not found actions"
        }
        //add owner as managaer=>push user visible and checked
        data.users["checked"][userId] = actionsNames;
        data.users["visibles"].push({
            id: userId,
            username: userName,
            type: "user"
        })
    }

    async onSubmit(shared: any): Promise<void> {
        let invitees: Whitelist[] = [];
        Object.keys(shared.users).forEach(function (userId: string) {
            invitees.push({id: userId})
        });
        Object.keys(shared.bookmarks).forEach(function (groupId: string) {
            invitees.push({id: groupId, isGroup: true})
        });
        Object.keys(shared.groups).forEach(function (groupId: string) {
            invitees.push({id: groupId, isGroup: true})
        });
        this.worlds.all.forEach((world: IWorld) => {
            world.whitelist = world.whitelist ? world.whitelist.concat(invitees) : invitees;
            world.subject = idiom.translate('minetest.invitation.default.subject');
            minetestService.invite(world)
                .then((world: IWorld) => {
                    world.whitelist = world.whitelist;
                    toasts.confirm('minetest.world.invite.confirm');
                    safeApply(this.$scope);
                }).catch(() => {
                toasts.warning('minetest.world.invite.error');
            });
        });
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}share-world/share-world.html`,
        scope: {
            worlds: '=',
            onShareWorld: '&'
        },
        controllerAs: 'vm',
        bindToController: true,
        controller: ['$scope', Controller],
        link: function (scope: ng.IScope,
                        element: ng.IAugmentedJQuery,
                        attrs: ng.IAttributes,
                        vm: ng.IController) {

        }
    }
}
export const shareWorld = ng.directive('shareWorld', directive);