import {moment, ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import {minetestService} from "../../services";
import {AxiosError} from "axios";

interface IViewModel {
    openPropertiesWorldLightbox(): void;
    closePropertiesLightbox(): void;
    updateWorld(): Promise<void>;

    lightbox: any;

    // props
    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;

    world: IWorld;

    constructor(private $scope: IScope) {
        {
            this.lightbox = {
                properties: false,
            };
        }
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openPropertiesWorldLightbox(): void {
        this.lightbox.properties = true;
    }

    closePropertiesLightbox(): void {
        this.lightbox.properties = false;
    }

    async updateWorld(): Promise<void> {
        let world: IWorld = {
            owner_id: this.world.owner_id,
            owner_name: this.world.owner_name,
            owner_login: this.world.owner_login,
            created_at: this.world.created_at,
            updated_at: moment().startOf('day')._d,
            password: this.world.password,
            status: false,
            title: this.world.title,
            selected: false,
            img: this.world.img,
            shared: this.world.shared,
            address: this.world.address
        }
        minetestService.update(this.world)
            .then(() => {
                toasts.confirm('minetest.world.create.confirm');
                this.closePropertiesLightbox();
                this.$scope.$eval(this.$scope['vm']['onCreateWorld']());
            }).catch((err: AxiosError) => {
            toasts.warning('minetest.world.create.error');
        })
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}properties-world/properties-world.html`,
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
export const propertiesWorld = ng.directive('propertiesWorld', directive)