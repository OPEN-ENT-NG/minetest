import { ng } from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";

interface IViewModel {
    lightbox: any;

    // props
    world: IWorld;

    showTable(): boolean;
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
        this.$scope.$eval(this.$scope['vm']['onFollowWorld']);
    }

    closeFollowLightbox(): void {
        this.lightbox.follow = false;
    }

    showTable(): boolean {
        return !this.world.whitelist || this.world.owner_login != this.world.whitelist[0];
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}follow-world/follow-world.html`,
        scope: {
            world: '=',
            $onFollowWorld: '&'
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