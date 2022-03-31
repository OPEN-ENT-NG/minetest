import {ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import {minetestService} from "../../services";

interface IViewModel {
    openDeleteWorldLightbox(): void;
    closeDeleteLightbox(): void;
    deleteWorld(): Promise<void>;
    lightbox: any;

    // props
    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;

    world: IWorld;

    constructor(private $scope: IScope)
    {
        this.lightbox = {
            delete: false,
        };
    }

    $onInit() {
    }

    $onDestroy() {
    }

    closeDeleteLightbox(): void {
        this.lightbox.delete = false;
    }

    async deleteWorld(): Promise<void> {
        let response = await minetestService.delete(this.world);
        if (response) {
            toasts.confirm('minetest.world.delete.confirm');
            this.closeDeleteLightbox();
            this.$scope.$eval(this.$scope['vm']['onDeleteWorld']());
        } else {
            toasts.warning('minetest.world.delete.error');
        }
    }

    openDeleteWorldLightbox(): void {
        this.lightbox.delete = true;
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}delete-world/delete-world.html`,
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
export const deleteWorld = ng.directive('deleteWorld', directive)