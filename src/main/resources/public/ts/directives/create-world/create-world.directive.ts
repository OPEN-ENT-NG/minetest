import {model, moment, ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import {DateUtils} from "../../utils/date.utils";
import {minetestService} from "../../services";

interface IViewModel {
    openCreateLightbox();
    closeCreateLightbox();
    createWorld();
    uploadFile();

    lightbox: any;
    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IWorld;

    constructor(private $scope: IScope)
    {
        this.lightbox = {
            create: false,
            sharing: false,
            invitation: false,
            delete: false,
        };
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openCreateLightbox(): void {
        this.lightbox.create = true;
    }

    closeCreateLightbox(): void {
        this.lightbox.create = false;
    }

    async createWorld(): Promise<void> {
        this.world = {
            owner_id:  model.me.userId,
            owner_name: model.me.username,
            created_at: DateUtils.format(moment().startOf('minute'), "DD/MM/YYYY HH:mm"),
            updated_at: DateUtils.format(moment().startOf('minute'), "DD/MM/YYYY HH:mm"),
            password: this.world.password,
            status: false,
            title: this.world.title,
            selected: false
        }

        let response = await minetestService.create(this.world);
        if (response) {
            toasts.confirm('minetest.world.create.confirm');
            this.closeCreateLightbox();
            this.$scope.$eval(this.$scope['vm']['onCreateWorld']());
            this.$scope.$apply();
        } else {
            toasts.warning('minetest.world.create.error');
        }
    }

    uploadFile(): void {

    }

}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}create-world/create-world.html`,
        scope: {
            onCreateWorld: '&'
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
export const createWorld = ng.directive('createWorld', directive)