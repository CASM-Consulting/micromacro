const queryNotes = {
    templateUrl: 'html/components/notes.html',
    bindings: {
        notes: '<'
    },
    controller : function($scope, $element, $document, $window, $timeout, Queries, $stateParams) {

        $scope.show = true;

        var $ctrl = this;
        var elem = $element;

        var saveNotes = $ctrl.onChange = $scope.onChange = () => {
            Queries.setMeta($stateParams.workspaceId, $stateParams.queryId, "notes", $ctrl.notes);
        };

        $ctrl.$onInit = () => {
            //            alert($ctrl.notes);
        }

        $scope.addNote = function(note){
            $scope.showNotes = true;
            if (!$ctrl.notes){
                $ctrl.notes = [];
            }
            $ctrl.notes.push(note || {"w":140,"x":400,"h":75,"y":400,"text":""});
            saveNotes();

            //            focusNewNote();
        };


        // Necessary for updating positions correctly after a note is removed from lower in the list
        $scope.getNoteShape = function(idx) {
            return {
                top: $ctrl.notes[idx].y,
                left: $ctrl.notes[idx].x,
                width: $ctrl.notes[idx].w,
                height: $ctrl.notes[idx].h,
                'background-color': $ctrl.notes[idx].colour || '#ffd59b'
            };
        };

        $scope.copyNote = function(idx) {
            var xOffset = 30; // Npx to the right of original
            var yOffset = 30; // Npx to the right of original

            var newNote = {
                x: $ctrl.notes[idx].x + xOffset,
                y: $ctrl.notes[idx].y + yOffset,
                w: $ctrl.notes[idx].w,
                h: $ctrl.notes[idx].h,
                text: $ctrl.notes[idx].text,
            };
            if ($ctrl.notes[idx].colour){
                newNote.colour = $ctrl.notes[idx].colour;
            }
            $ctrl.notes.push(newNote);
            $timeout(function(){
                $scope.focusNote($ctrl.notes.length - 1);
            })
            $scope.onChange();
        };

        $scope.removeNote = function(idx) {

            if ($ctrl.notes[idx].text && $ctrl.notes[idx].text.trim().length) {
                var del = confirm("Are you sure you want to permanently delete this note and its contents?");
                if(del) {
                    $ctrl.notes.splice(idx, 1);
                    $scope.onChange();
                }
                //                    var modal = Modals.open("confirm", {
                //                        title: "Delete note?",
                //                        message: "Are you sure you want to permanently delete this note and its contents?",
                //                        confirmText: "Delete"
                //                    });
                //                    modal.on("confirm", function () {
                //                        $ctrl.notes.splice(idx, 1);
                //                        $scope.onChange();
                //                    });
            } else {
                // Note empty, don't ask for confirmation...
                $ctrl.notes.splice(idx, 1);
                $scope.onChange();
            }
        };

        $ctrl.focusNote = function(idx) {
            angular.forEach(elem.find(".basic-note"), function(note, noteIndex){
                var noteElem = angular.element(note);
                noteElem.css({"z-index": noteIndex==idx? 100 : 90});
            });
        };

        $ctrl.focusNoteElem = function(noteElem1) {
            angular.forEach(elem.find(".basic-note"), function(note, noteIndex){
                var noteElem2 = angular.element(note);
                noteElem2.css({"z-index": noteElem1[0] == noteElem2[0]? 100 : 90});
            });
        };
    }
};

export default queryNotes;
