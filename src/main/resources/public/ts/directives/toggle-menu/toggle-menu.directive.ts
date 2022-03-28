import {ng} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld, Worlds} from "../../models";

interface IViewModel {
    // getNbSelectedWorld(): number;

    // props
    // worlds: Worlds;
    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    // worlds: Worlds;
    world: IWorld;

    constructor(private $scope: IScope) {

    }

    $onInit() {
        console.log("toggle" + " " + this.world)
    }

    $onDestroy() {
    }

    // getNbSelectedWorld(): number {
    //     let selectedWorld: number = 0;
    //     this.worlds.all.forEach((world: IWorld) => {
    //         if (world.selected) selectedWorld++;
    //     });
    //     return selectedWorld;
    // }

}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}toggle-menu/toggle-menu.html`,
        scope: {
            world: '=',
            onDeleteWorld: '&'
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
export const toggleMenu = ng.directive('toggleMenu', directive)