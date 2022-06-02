import {ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import {minetestService} from "../../services";

interface IViewModel {
    openDeleteLightbox(): void;
    closeDeleteLightbox(): void;
    deleteWorld(): Promise<void>;
    deleteImportWorld(): Promise<void>;

    lightbox: any;

    // props
    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IWorld;

    constructor(private $scope: IScope) {
        this.lightbox = {
            delete: false,
        };
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openDeleteLightbox(): void {
        this.lightbox.delete = true;
    }

    closeDeleteLightbox(): void {
        this.lightbox.delete = false;
    }

    async deleteWorld(): Promise<void> {
        minetestService.delete(this.world)
            .then(() => {
                toasts.confirm('minetest.world.delete.confirm');
                this.closeDeleteLightbox();
                this.$scope.$eval(this.$scope['vm']['onDeleteWorld']());
            }).catch(() => {
            toasts.warning('minetest.world.delete.error');
        })
    }

    async deleteImportWorld(): Promise<void> {
        minetestService.deleteImportWorld(this.world)
            .then(() => {
                toasts.confirm('minetest.world.delete.confirm');
                this.closeDeleteLightbox();
                this.$scope.$eval(this.$scope['vm']['onDeleteWorld']());
            }).catch(() => {
            toasts.warning('minetest.world.delete.error');
        })
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
export const deleteWorld = ng.directive('deleteWorld', directive);