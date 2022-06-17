import { ng } from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";

interface IViewModel {
    lightbox: any;

    // props
    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IWorld;

    constructor(private $scope: IScope) {
        {
            this.lightbox = {
                follow: false,
            };
        }
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openFollowLightbox(): void {
        this.lightbox.follow = true;
    }

    closeFollowLightbox(): void {
        this.lightbox.follow = false;
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}follow-world/follow-world.html`,
        scope: {
            world: '='
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
export const followWorld = ng.directive('followWorld', directive)