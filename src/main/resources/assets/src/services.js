import {DatumFactory} from './ts/DatumFactory';
import collapse from './services/collapse';
import datums from './services/datums';
import filters from './services/filters';
import maps from './services/maps';
import queries from './services/queries';
import rows from './services/rows';
import tables from './services/tables';
import types from './services/types';
import server from './services/server';
import workspaces from './services/workspaces';
import spans from './services/spans';

function initializeServices(MicroMacroApp) {
    console.log("initializing services");

    MicroMacroApp.factory("Collapse", collapse);
    MicroMacroApp.factory("Datums", datums);
    MicroMacroApp.factory("Filters", filters);
    MicroMacroApp.factory("Maps", maps);
    MicroMacroApp.factory("Queries", queries);
    MicroMacroApp.factory("Rows", rows);
    MicroMacroApp.factory("Spans", spans);
    MicroMacroApp.factory("Tables", tables);
    MicroMacroApp.factory("Types", types);
    MicroMacroApp.factory("Server", server);
    MicroMacroApp.factory("Workspaces", workspaces);
}

export {initializeServices};
