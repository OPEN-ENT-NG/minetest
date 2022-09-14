import {ng} from 'entcore';

export const LoaderDirective = ng.directive('loader', () => {
    return {
        restrict: 'E',
        scope: {
            title: '=',
            minHeight: '='
        },
        template: `
        <div class="loader-container" ng-style="{'min-height': minHeight}">
            <div>
                <div class="loader"></div>
                <p>[[title]]</p>
            </div>
        </div>
        `,
    };
});
