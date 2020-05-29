'use strict';

const ClassResponse = {
  bindings: {
    response: '<',
    componentannotations: '<',
    mode: '@',
    deletebuttonclicked: '&',
    undodeletebuttonclicked: '&',
    submitbuttonclicked: '&',
    createupvoteannotation: '&',
    createdownvoteannotation: '&',
    createunvoteannotation: '&',
    studentdatachanged: '&',
    isdisabled: '<',
    isvotingallowed: '<'
  },
  templateUrl: 'wise5/components/discussion/classResponse.html',
  controller: 'ClassResponseController as classResponseCtrl'
};

export default ClassResponse;
