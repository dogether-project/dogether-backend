import {createLocalDbConnection, createSshTunnelDbConnection} from "../db/util/db-util.js";
// import { createDummyData } from "../db/data/dummy-data/dummy-data-1.js";
import { createDummyData } from "../db/data/dummy-data/dummy-data-2.js";
import {insertData} from "../db/util/data-insert-runner.js";

async function main() {
    const connection = await createLocalDbConnection(); // Local DB 커넥션
    // const connection = await createSshTunnelDbConnection(); // AWS DB 커넥션

    const dummyData = createDummyData();

    await insertData(connection, dummyData);
}

main();
