import {_, idiom, moment, ng, toasts} from "entcore";
import {IScope} from "angular";
import {RootsConst} from "../../core/constants/roots.const";
import {IWorld, Whitelist} from "../../models";
import {minetestService} from "../../services";
import {safeApply} from "../../utils/safe-apply.utils";
import {DateUtils} from "../../utils/date.utils";
import {Subject} from "rxjs";
import {ToggleMenuAction} from "../toggle-menu/toggle-menu-action.model";
import {TOGGLE_MENU_ACTION} from "../toggle-menu/toggle-menu-action.enum";

declare let window: any;

interface IViewModel {
    openInvitationLightbox(): void;
    closeInvitationLightbox(): void;
    sendInvitation(): Promise<void>;


    lightbox: any;

    mail: {
        subject: string,
        body: string,
        invitees: Whitelist[]
    };

    downloadLink: string;

    // props
    world: IWorld;
    eventer: Subject<ToggleMenuAction>;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IWorld;
    downloadLink: string;
    mail: {
        subject: string,
        body: string,
        invitees: Whitelist[]
    };
    eventer: Subject<ToggleMenuAction>;

    constructor(private $scope: IScope) {
        this.lightbox = {
            invitation: false,
        };
        this.mail = {
            subject: "",
            body: "",
            invitees: [],
        };
    }

    $onInit() {
       this.downloadLink = window.minetestDownload;
        this.eventer.asObservable().subscribe((action: ToggleMenuAction) => {
            if (action && action.actionComponentType === TOGGLE_MENU_ACTION.INVITATION_WORLD) {
                this.openInvitationLightbox();
            }
        });
    }

    $onDestroy() {
    }

    openInvitationLightbox(): void {
        this.lightbox.invitation = true;
        this.initMail();
    }

    closeInvitationLightbox(): void {
        this.lightbox.invitation = false;
    }

    async sendInvitation(): Promise<void> {
        this.world.updated_at = DateUtils.format(moment().startOf('minute'),
            DateUtils.FORMAT["DAY/MONTH/YEAR-HOUR-MIN"]);
        this.world.whitelist = this.world.whitelist ? this.world.whitelist.concat(this.mail.invitees) : this.mail.invitees;
        this.world.subject = this.mail.subject;
        this.closeInvitationLightbox();
        minetestService.invite(this.world)
            .then((world:IWorld) => {
                this.world.whitelist = world.whitelist;
                toasts.confirm('minetest.world.invite.confirm');
                safeApply(this.$scope);
            }).catch(() => {
            toasts.warning('minetest.world.invite.error');
        });
    }

    initMail(): void {
        this.mail.subject = idiom.translate('minetest.invitation.default.subject');
        if (!this.world.password) {
            this.mail.body = idiom.translate('minetest.invitation.default.body.1')
                    .replaceAll("<mettre lien>", this.downloadLink) +
                idiom.translate('minetest.invitation.default.body.address') + this.world.address +
                idiom.translate('minetest.invitation.default.body.port') + this.world.port +
                idiom.translate('minetest.invitation.default.body.end');
        } else
            this.mail.body = idiom.translate('minetest.invitation.default.body.1')
                    .replaceAll("<mettre lien>", this.downloadLink) +
                idiom.translate('minetest.invitation.default.body.address') + this.world.address +
                idiom.translate('minetest.invitation.default.body.port') + this.world.port +
                idiom.translate('minetest.invitation.default.body.name') + "user_login" +
                idiom.translate('minetest.invitation.default.body.password') + this.world.password +
                idiom.translate('minetest.invitation.default.body.end');
        this.mail.invitees = [];
    }

}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}invitation-world/invitation-world.html`,
        scope: {
            world: '=',
            eventer: '='
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
export const invitationWorld = ng.directive('invitationWorld', directive)