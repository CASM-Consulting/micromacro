import collapseZone from './directives/collapse-zone';
import mCollapse from './directives/m-collapse';
import collapsible from './directives/collapsible';
import key from './directives/key';
import keySelector from './directives/key-selector';
import lineChart from './directives/line-chart';
import orderBy from './directives/order-by';
import partition from './directives/partition';

function initializeDirectives(MicroMacroApp) {
    console.log("initializing directives");

    MicroMacroApp.directive("collapseZone", collapseZone);
    MicroMacroApp.directive("mCollapse", mCollapse);
    MicroMacroApp.directive("collapsible", collapsible);
    MicroMacroApp.directive("key", key);
    MicroMacroApp.directive('keySelector', keySelector);
    MicroMacroApp.directive("lineChart", lineChart);
    MicroMacroApp.directive("orderBy", orderBy);
    MicroMacroApp.directive("partition", partition);
}

export {initializeDirectives};

