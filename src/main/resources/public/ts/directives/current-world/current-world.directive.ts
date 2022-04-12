import {idiom, moment, ng, notify, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import * as Clipboard from 'clipboard';
import {minetestService} from "../../services";
import {AxiosError} from "axios";
import {DateUtils} from "../../utils/date.utils";

interface IViewModel {
    setStatusWorld(currentWorld: IWorld): void;
    setLegendLightboxVisible(): void;

    isWorldValid(): boolean;


    // props
    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    world: IWorld;
    isLegendVisible: boolean;

    constructor(private $scope: IScope) {

    }

    $onInit() {
        //copy link
        let clipboardLink = new Clipboard('.clipboard-link-field');
        clipboardLink.on('success', function(e) {
            e.clearSelection();
            notify.info('minetest.copy.link.success');
        });
        clipboardLink.on('error', function(e) {
            notify.error('minetest.copy.link.error');
        });

        //copy port
        let clipboardPort = new Clipboard('.clipboard-port-field');
        clipboardPort.on('success', function(e) {
            e.clearSelection();
            notify.info('minetest.copy.port.success');
        });
        clipboardPort.on('error', function(e) {
            notify.error('minetest.copy.port.error');
        });

        this.isLegendVisible = false;
    }

    $onDestroy() {
    }

    isWorldValid(): boolean {
        return this.world !== undefined && this.world !== null && Object.keys(this.world).length > 0;
    }

    setStatusWorld(currentWorld: IWorld): void {
        this.world.status = !currentWorld.status;
        this.world.updated_at = DateUtils.format(moment().startOf('minute'), "DD/MM/YYYY HH:mm");
        minetestService.updateStatus(this.world)
            .then(() => {
                notify.info(idiom.translate('minetest.world.open.confirm'));
                if(this.world.status) {
                    notify.info('minetest.world.open.confirm');
                    toasts.confirm('minetest.world.open.confirm');
                }
                else
                    notify.info('minetest.world.close.confirm');
                    // toasts.confirm('minetest.world.close.confirm');
                this.$scope.$eval(this.$scope['vm']['onCurrentWorld']());
            }).catch((err: AxiosError) => {
            toasts.warning('minetest.world.open.close.error');
        })
    }

    setLegendLightboxVisible(): void {
        this.isLegendVisible = !this.isLegendVisible;
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