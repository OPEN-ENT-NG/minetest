import {ng, notify} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import * as Clipboard from 'clipboard';

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
        let clipboard = new Clipboard('.clipboard-link-field');
        clipboard.on('success', function(e) {
            e.clearSelection();
            notify.info('copy.link.success');
        });
        clipboard.on('error', function(e) {
            notify.error('copy.link.error');
        });
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
            world: '=',
            onCurrentWorld: '&'
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