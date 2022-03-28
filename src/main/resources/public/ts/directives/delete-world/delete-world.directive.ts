import {ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld, Worlds} from "../../models";
import {minetestService} from "../../services";

interface IViewModel {
    openDeleteWorldLightbox(): void;
    closeDeleteLightbox(): void;
    deleteWorld(): Promise<void>;
    // getSelectedWorlds(): Array<IWorld>;
    lightbox: any;

    // props
    world: IWorld;
    // worlds: Worlds;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    // selectedWorld: Array<IWorld>;

    world: IWorld;
    // worlds: Worlds;

    constructor(private $scope: IScope)
    {
        this.lightbox = {
            delete: false,
        };
    }

    $onInit() {
        console.log("delete" + " " + this.world)
        // this.selectedWorld = this.worlds.all.filter((world: IWorld) => world.selected);
    }

    // getSelectedWorlds(): Array<IWorld> {
    //     return this.worlds.all.filter((world: IWorld) => world.selected);
    // }

    $onDestroy() {
    }

    closeDeleteLightbox(): void {
        this.lightbox.delete = false;
    }

    async deleteWorld(): Promise<void> {
        // let selectedWorldIs = Array<String>();

        // this.worlds.all
        //     .filter((world: IWorld) => world.selected)
        //     .forEach(world => {
        //         if (world.selected) selectedWorldIs.push(world._id);
        //     })

        // let response = await minetestService.delete(selectedWorldIs);
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