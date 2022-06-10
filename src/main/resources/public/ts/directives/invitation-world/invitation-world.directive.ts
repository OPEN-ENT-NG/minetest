import {_, idiom, ng, toasts} from "entcore";
import {IScope} from "angular";
import {RootsConst} from "../../core/constants/roots.const";
import {IWorld, User, Users} from "../../models";
import {minetestService} from "../../services";


interface IViewModel {
    openInvitationLightbox(): void;
    closeInvitationLightbox(): void;
    sendInvitation(): Promise<void>;


    lightbox: any;

    mail: {
        subject: string,
        body: string,
        invitees: string[]
    };

    // props
    world: IWorld;
    downloadLink: string;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IWorld;
    downloadLink: string;
    mail: {
        subject: string,
        body: string,
        invitees: string[]
    };

    constructor(private $scope: IScope) {
        {
            this.lightbox = {
                invitation: false,
            };
            this.mail = {
                subject: "",
                body: "",
                invitees: [],
            };
        }
    }

    $onInit() {
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
        this.world.whitelist = this.mail.invitees;
        this.world.subject = this.mail.subject;
        minetestService.invite(this.world)
            .then(() => {
                toasts.confirm('minetest.world.invite.confirm');
                this.closeInvitationLightbox();
                this.$scope.$eval(this.$scope['vm']['onDeleteWorld']());
            }).catch(() => {
            toasts.warning('minetest.world.invite.error');
        })
    }

    updateFoundUsers = async (search, model, founds) => {
        let include = [];
        const exclude = model || [];
        new Users().findUser(search, include, exclude)
            .then((users) => {
                Object.assign(founds, users, { length: users.length });
            }).catch(() => {
            toasts.warning('minetest.world.invite.error');
        })
    };

    initMail(): void {
        this.mail.subject = idiom.translate('minetest.invitation.default.subject');
        if (!this.world.password) {
            this.mail.body = idiom.translate('minetest.invitation.default.body.1')
                    .replace("<mettre lien>", this.downloadLink) +
                idiom.translate('minetest.invitation.default.body.address') + this.world.address +
                idiom.translate('minetest.invitation.default.body.port') + this.world.port +
                idiom.translate('minetest.invitation.default.body.end');
        } else
            this.mail.body = idiom.translate('minetest.invitation.default.body.1')
                    .replace("<mettre lien>", this.downloadLink) +
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
            downloadLink: '=',
            onInvitationWorld: '&'
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