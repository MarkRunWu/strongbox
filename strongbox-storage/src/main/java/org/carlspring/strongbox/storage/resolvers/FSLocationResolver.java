package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.*;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class FSLocationResolver
        implements LocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(FSLocationResolver.class);

    private String alias = "file-system";

    @Autowired
    private ConfigurationManager configurationManager;

    private DataCenter dataCenter = new DataCenter();


    public FSLocationResolver()
    {
    }

    @Override
    public InputStream getInputStream(String repository,
                                      String artifactPath)
            throws IOException
    {
        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repository))
            {
                logger.debug("Checking in storage " + storage.getBasedir() + "...");

                final Map<String, Repository> repositories = storage.getRepositories();

                for (String key : repositories.keySet())
                {
                    Repository r = repositories.get(key);

                    logger.debug("Checking in repository " + r.getName() + "...");

                    final File repoPath = new File(storage.getBasedir(), r.getName());
                    final File artifactFile = new File(repoPath, artifactPath).getCanonicalFile();

                    logger.debug("Checking for " + artifactFile.getCanonicalPath() + "...");

                    if (artifactFile.exists())
                    {
                        logger.info("Resolved " + artifactFile.getCanonicalPath() + "!");

                        return new FileInputStream(artifactFile);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public OutputStream getOutputStream(String repository,
                                        String artifactPath)
            throws IOException
    {
        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repository))
            {
                final Map<String, Repository> repositories = storage.getRepositories();

                Repository r = repositories.get(repository);

                final File repoPath = new File(storage.getBasedir(), r.getName());
                final File artifactFile = new File(repoPath, artifactPath).getCanonicalFile();

                if (!artifactFile.getParentFile().exists())
                {
                    logger.debug("Creating base dir for artifact " + artifactFile.getCanonicalPath() + "...");

                    //noinspection ResultOfMethodCallIgnored
                    artifactFile.getParentFile().mkdirs();
                }

                return new FileOutputStream(artifactFile);
            }
        }

        return null;
    }

    @Override
    public void initialize()
            throws IOException
    {
        final Map<String, Storage> storages = configurationManager.getConfiguration().getStorages();

        dataCenter.setStorages(storages);

        logger.info("Initialized FSLocationResolver.");
    }

    @Override
    public String getAlias()
    {
        return alias;
    }

    @Override
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public DataCenter getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter)
    {
        this.dataCenter = dataCenter;
    }

}
