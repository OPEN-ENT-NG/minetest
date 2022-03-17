import {ng} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";

interface IViewModel {
    setStatusWorld(currentWorld: IWorld): void;

    isWorldValid(): boolean;

    // props
    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    world: IWorld;

    constructor(private $scope: IScope) {

    }

    $onInit() {
    }

    $onDestroy() {
    }

    isWorldValid(): boolean {
        return this.world !== undefined && this.world !== null && Object.keys(this.world).length > 0;
    }

    setStatusWorld(currentWorld: IWorld): void {
        this.world.status = !currentWorld.status;
    }

}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}current-world/current-world.html`,
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
export const currentWorld = ng.directive('currentWorld', directive)