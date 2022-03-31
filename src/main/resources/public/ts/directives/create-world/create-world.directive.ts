import {model, moment, ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import {DateUtils} from "../../utils/date.utils";
import {minetestService} from "../../services";
import {AxiosError} from "axios";

interface IViewModel {
    openCreateLightbox();
    closeCreateLightbox();
    createWorld();

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
            owner_login: model.me.login,
            created_at: DateUtils.format(moment().startOf('minute'), "DD/MM/YYYY HH:mm"),
            updated_at: DateUtils.format(moment().startOf('minute'), "DD/MM/YYYY HH:mm"),
            password: this.world.password,
            status: false,
            img: this.world.img,
            title: this.world.title,
            selected: false,
            address: "minetest.support-ent.fr"
        }
        minetestService.create(this.world)
            .then(() => {
                toasts.confirm('minetest.world.create.confirm');
                this.closeCreateLightbox();
                this.$scope.$eval(this.$scope['vm']['onCreateWorld']());
            }).catch((err: AxiosError) => {
            toasts.warning('minetest.world.create.error');

        })
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