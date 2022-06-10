import { ng } from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";

interface IViewModel {
    lightbox: any;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;

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
            onFollowWorld: '&'
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