"use strict";

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var StepToolsCtrl = function () {
    function StepToolsCtrl($scope, NodeService, ProjectService, StudentDataService) {
        _classCallCheck(this, StepToolsCtrl);

        this.$scope = $scope;
        this.NodeService = NodeService;
        this.ProjectService = ProjectService;
        this.StudentDataService = StudentDataService;

        this.nodeStatuses = this.StudentDataService.nodeStatuses;
        this.nodeStatus = this.nodeStatuses[this.nodeId];

        this.prevId = this.NodeService.getPrevNodeId();
        this.nextId = this.NodeService.getNextNodeId();

        // service objects and utility functions
        this.idToOrder = this.ProjectService.idToOrder;

        // model variable for selected node id
        this.toNodeId = this.nodeId;

        var scope = this;
        this.$scope.$watch(function () {
            return scope.toNodeId;
        }.bind(this), function (newId, oldId) {
            if (newId !== oldId) {
                // selected node id has changed, so open new node
                this.StudentDataService.endCurrentNodeAndSetCurrentNodeByNodeId(newId);
            }
        }.bind(this));
    }

    _createClass(StepToolsCtrl, [{
        key: 'getTemplateUrl',
        value: function getTemplateUrl() {
            return this.ProjectService.getThemePath() + '/node/stepTools.html';
        }
    }, {
        key: 'getNodeTitleByNodeId',
        value: function getNodeTitleByNodeId(nodeId) {
            return this.ProjectService.getNodeTitleByNodeId(nodeId);
        }
    }, {
        key: 'getNodePositionById',
        value: function getNodePositionById(nodeId) {
            return this.ProjectService.getNodePositionById(nodeId);
        }
    }, {
        key: 'isGroupNode',
        value: function isGroupNode(nodeId) {
            return this.ProjectService.isGroupNode(nodeId);
        }
    }, {
        key: 'goToPrevNode',
        value: function goToPrevNode() {
            this.NodeService.goToPrevNode();
        }
    }, {
        key: 'goToNextNode',
        value: function goToNextNode() {
            this.NodeService.goToNextNode();
        }
    }, {
        key: 'closeNode',
        value: function closeNode() {
            this.NodeService.closeNode();
        }
    }]);

    return StepToolsCtrl;
}();

StepToolsCtrl.$inject = ['$scope', 'NodeService', 'ProjectService', 'StudentDataService'];

exports.default = StepToolsCtrl;
//# sourceMappingURL=stepToolsController.js.map