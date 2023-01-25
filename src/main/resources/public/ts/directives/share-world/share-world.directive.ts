import {model, ng} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld, Worlds} from "../../models";

interface IViewModel {
    openShareLightbox(): void;
    closeShareLightbox(): void;
    canEditShareItem(args: any) : boolean;
    onShareFeed(data: any, resource: IWorld, actions: any[]) : void;

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