'use strict';

import * as angular from 'angular';
import { downgradeInjectable } from '@angular/upgrade/static';
import { EmbeddedService } from './embeddedService';
import EmbeddedController from './embeddedController';
import EmbeddedAuthoringController from './embeddedAuthoringController';

const embeddedAuthoringComponentModule = angular
  .module('embeddedAuthoringComponentModule', ['pascalprecht.translate'])
  .service('EmbeddedService', downgradeInjectable(EmbeddedService))
  .controller('EmbeddedController', EmbeddedController)
  .controller('EmbeddedAuthoringController', EmbeddedAuthoringController)
  .config([
    '$translatePartialLoaderProvider',
    $translatePartialLoaderProvider => {
      $translatePartialLoaderProvider.addPart('components/embedded/i18n');
    }
  ]);

export default embeddedAuthoringComponentModule;
