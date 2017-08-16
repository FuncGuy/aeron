/*
 * Copyright 2014-2017 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.aeron.archive;

import io.aeron.driver.Configuration;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.ShutdownSignalBarrier;

import static io.aeron.driver.MediaDriver.loadPropertiesFiles;

/**
 * Archiving {@link MediaDriver}.
 */
public final class ArchivingMediaDriver implements AutoCloseable
{
    private final MediaDriver driver;
    private final Archive archive;

    private ArchivingMediaDriver(final MediaDriver driver, final Archive archive)
    {
        this.driver = driver;
        this.archive = archive;
    }

    /**
     * Get the launched {@link Archive}.
     *
     * @return the launched {@link Archive}.
     */
    public Archive archive()
    {
        return archive;
    }

    /**
     * Get the launched {@link MediaDriver}.
     *
     * @return the launched {@link MediaDriver}.
     */
    public MediaDriver mediaDriver()
    {
        return driver;
    }

    public void close()
    {
        archive.close();
        driver.close();
    }

    /**
     * Launch an {@link Archive} with an embedded {@link MediaDriver} and await a shutdown signal.
     *
     * @param args command line argument which is a list for properties files as URLs or filenames.
     */
    public static void main(final String[] args)
    {
        loadPropertiesFiles(args);

        try (ArchivingMediaDriver ignore = launch())
        {
            new ShutdownSignalBarrier().await();

            System.out.println("Shutdown Archive...");
        }
    }

    /**
     * Launch a new {@link ArchivingMediaDriver} with default contexts.
     *
     * @return a new {@link ArchivingMediaDriver} with default contexts.
     */
    public static ArchivingMediaDriver launch()
    {
        return launch(new MediaDriver.Context(), new Archive.Context());
    }

    /**
     * Launch a new {@link ArchivingMediaDriver} with provided contexts.
     *
     * @param driverCtx  for configuring the {@link MediaDriver}.
     * @param archiveCtx for configuring the {@link Archive}.
     * @return a new {@link ArchivingMediaDriver} with the provided contexts.
     */
    public static ArchivingMediaDriver launch(final MediaDriver.Context driverCtx, final Archive.Context archiveCtx)
    {
        final boolean useConcurrentCounterManager =
            driverCtx.threadingMode() != ThreadingMode.INVOKER ||
                (driverCtx.threadingMode() == null && Configuration.THREADING_MODE_DEFAULT != ThreadingMode.INVOKER);

        final MediaDriver driver = MediaDriver.launch(driverCtx
            .useConcurrentCounterManager(useConcurrentCounterManager));

        final Archive archive = Archive.launch(archiveCtx
                .mediaDriverAgentInvoker(driver.sharedAgentInvoker())
                .countersManager(driver.context().countersManager())
                .errorHandler(driver.context().errorHandler()));

        return new ArchivingMediaDriver(driver, archive);
    }
}
