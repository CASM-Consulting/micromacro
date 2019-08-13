// Modeled on componentBox

const basicNote = {
    bindings: {
        idx : "<"
    },
    require: {
        notesCtrl : '^queryNotes'
    },
    controller : function ($scope, $element, $attrs, $document, $timeout){
        var scope = $scope;
        var elem = $element;
        var attrs = $attrs;
        var $ctrl = this;

        var idx;

        var MIN_HEIGHT = 28;
        var MIN_WIDTH = 40;

        var props;

        var loaded = false;

        $ctrl.$onInit = () => {
            //            alert($ctrl.notes);
            idx = parseInt($ctrl.idx)

            props = $ctrl.notesCtrl.notes[idx];

            canvasW = elem.parent().parent().parent().width();
            canvasH = elem.parent().parent().parent().parent().height();
            // Because the ng-style keeps these up to date, so trust them over props

            props = limitPos(props);

            applyProps();

        }


        var applyProps = function(){
            elem.css({
                top: props.y,
                left: props.x,
                width: props.w,
                height: props.h
            });
            var newProps = angular.copy(props);
            newProps["text"] = $ctrl.notesCtrl.notes[idx]["text"];
            newProps["colour"] = $ctrl.notesCtrl.notes[idx]["colour"];
            $ctrl.notesCtrl.notes[idx] = newProps;
        }

        var unbind = scope.$watch("notes["+idx+"]", function (val) {
            if (val && !loaded) {
                loaded = true;
                props = val;
                elem.css({top: props.y, left: props.x, width: props.w, height: props.h});
                unbind(); // Once loaded, this doesn't seem to be necessary
            }
        });

        var downX;
        var downY;

        var downLeft;
        var downTop;

        var downW;
        var downH;

        var canvasW;
        var canvasH;

        scope.handleTabKey = function(e) {
            if (e.which === 9) {
                e.preventDefault();
                thisElem = angular.element(e.target);
                var start = e.target.selectionStart;
                var end = e.target.selectionEnd;
                props.text = thisElem.val().substring(0, start) + '    ' + thisElem.val().substring(end);
                thisElem.val(props.text);
                e.target.selectionStart = e.target.selectionEnd = start + 4;
                applyProps();
            }
        }

        var doDrag = function (fns) {
            return function (event) {
                $ctrl.notesCtrl.focusNoteElem(elem);

                downX = event.pageX;
                downY = event.pageY;
                downLeft = elem[0].offsetLeft;
                downTop = elem[0].offsetTop;
                //                    downW = elem.width();
                //                    downH = elem.height();
                // More accurate widths, the ones above were reporting a slightly too small number
                downW = elem[0].offsetWidth;
                downH = elem[0].offsetHeight;
                canvasW = elem.parent().parent().parent().width();
                canvasH = elem.parent().parent().parent().parent().height();
                // Because the ng-style keeps these up to date, so trust them over props
                props = {x: downLeft, y: downTop, w: downW, h: downH};

                $(window).one("mouseup", function () {
                    window.onmousemove = undefined;
                    $ctrl.notesCtrl.onChange();
                });
                window.onmousemove = function (event) {
                    var i = fns.length;
                    while (i--) {
                        fns[i](event);
                    }
                    applyProps();
                    return false;
                };
                return false;
            };
        };

        var limitPos = function(box){
            box.x = Math.max(10, Math.min(canvasW-box.w-5, Math.max(5, box.x)));
            box.y = Math.max(10, Math.min(canvasH-box.h-5, Math.max(5, box.y)));
            return box;
        };

        var onMove = function(event) {
            var diffX = event.pageX - downX;
            var diffY = event.pageY - downY;
            var newLeft = downLeft + diffX;
            var newTop = downTop + diffY;

            props = props || {y: 0, x : 0, w : 100, h : 100};

            var newProps = angular.copy(props);
            newProps.x = newLeft;
            newProps.y = newTop;

            props = limitPos(newProps);
        };

        var onResizeWest = function (event) {
            var diffX = downX - event.pageX;
            var newLeft = downLeft - diffX;
            var newWidth = downW + diffX;

            if (newLeft < 0) {
                newWidth += newLeft;
                newLeft = 0;
            }

            if (newWidth < MIN_WIDTH) {
                newLeft -= MIN_WIDTH - newWidth;
                newWidth = MIN_WIDTH;
            }

            props.x = Math.max(0, newLeft);
            props.w = newWidth;
        };

        var onResizeEast = function (event) {
            var diffX = event.pageX - downX;
            var newWidth = downW + diffX;

            if (downLeft + newWidth > canvasW) {
                newWidth = canvasW - downLeft;
            }

            if (newWidth  < MIN_WIDTH) {
                newWidth = MIN_WIDTH;
            }

            props.w = newWidth;
        };

        var onResizeNorth = function (event) {
            var diffY = downY - event.pageY;
            var newTop = downTop - diffY;
            var newHeight = downH + diffY;
            if (newTop < 0) {
                newHeight += newTop;
                newTop = 0;
            }

            if (newHeight < MIN_HEIGHT) {
                newTop -= MIN_HEIGHT-newHeight;
                newHeight = MIN_HEIGHT;
            }

            props.y = Math.max(0, newTop);
            props.h = newHeight;
        };

        var onResizeSouth = function (event) {
            var diffY = event.pageY - downY;
            var newHeight = downH + diffY;

            if (downTop + newHeight > canvasH) {
                newHeight = canvasH - downTop;
            }

            if (newHeight < MIN_HEIGHT) {
                newHeight = MIN_HEIGHT;
            }

            props.h = newHeight;
        };


        var isPopoverOpen = function(){
            var popoverScope = elem.find(".popover").scope();
            return popoverScope && popoverScope.isOpen? true : false;
        }

        var closeColorPopover = function(event){
            if (elem.find(".basic-note-colour")[0] != event.target) {
                var popoverScope = elem.find(".popover").scope();
                if (popoverScope && popoverScope.isOpen){
                    $timeout(function(){
                        elem.find(".basic-note-colour").click();
                    }, 0);
                }
                clickAwayReady = false;
                $document.off('mousedown', closeColorPopover);
            }
        };

        var clickAwayReady = false;
        elem.find(".basic-note-colour").on('click', function(){
            if (isPopoverOpen() && !clickAwayReady){
                $document.on('mousedown', closeColorPopover);
                clickAwayReady = true;
            }
        });

        elem.find(".basic-note-title ").on("mousedown", doDrag([onMove]));

        elem.find('.resize-ns.north').on('mousedown', doDrag([onResizeNorth]));
        elem.find('.resize-ns.south').on('mousedown', doDrag([onResizeSouth]));
        elem.find('.resize-we.west').on('mousedown', doDrag([onResizeWest]));
        elem.find('.resize-we.east').on('mousedown', doDrag([onResizeEast]));

        elem.find('.resize-diag.nw').on('mousedown', doDrag([onResizeNorth, onResizeWest]));
        elem.find('.resize-diag.ne').on('mousedown', doDrag([onResizeNorth, onResizeEast]));
        elem.find('.resize-diag.sw').on('mousedown', doDrag([onResizeSouth, onResizeWest]));
        elem.find('.resize-diag.se').on('mousedown', doDrag([onResizeSouth, onResizeEast]));

        elem.css({"z-index": 110});

    }
};

export default basicNote;
