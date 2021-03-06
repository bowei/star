package org.star_lang.star.code.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.star_lang.star.StarMain;
import org.star_lang.star.code.repository.CodeRepository.RepositoryListener;
import org.star_lang.star.code.repository.zip.ZipArchive;
import org.star_lang.star.code.repository.zip.ZipCodeRepository;
import org.star_lang.star.compiler.ErrorReport;
import org.star_lang.star.compiler.util.ApplicationProperties;
import org.star_lang.star.compiler.util.ComboIterator;
import org.star_lang.star.compiler.util.NullIterator;
import org.star_lang.star.data.value.ResourceURI;
import org.star_lang.star.resource.ResourceException;
import org.star_lang.star.resource.catalog.CatalogException;

/**
 * The CompositeRepository combines multiple repositories into one. This is usually done to combine
 * different types of repositories, such as memory based, directory based and zip-file based
 * repositories.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * @author fgm
 * 
 */

public class CompositeRepository extends AbstractCodeRepository implements RepositoryListener
{
  private final List<CodeRepository> subRepositories;
  private final CodeRepository tgt;
  private RepositoryNode generic;
  private final Map<ResourceURI, RepositoryNode> cache = new HashMap<ResourceURI, RepositoryNode>();

  public CompositeRepository(String classPath, CodeRepository tgt, boolean overwrite, ClassLoader loader,
      ErrorReport errors) throws RepositoryException
  {
    super(loader, errors);
    this.tgt = tgt;
    this.subRepositories = new ArrayList<CodeRepository>();
    setupRepositories(classPath, overwrite, loader);
  }

  public CompositeRepository(String classPath, boolean overwrite, ClassLoader loader, ErrorReport errors)
      throws RepositoryException
  {
    this(classPath, StarMain.standardRepository(), overwrite, loader, errors);
  }

  public CompositeRepository(String classPath, boolean overwrite, ErrorReport errors) throws RepositoryException
  {
    this(classPath, StarMain.standardRepository(), overwrite, Thread.currentThread().getContextClassLoader(), errors);
  }

  public CompositeRepository(List<CodeRepository> subRepositories, CodeRepository tgt, boolean overwrite,
      ClassLoader loader, ErrorReport errors) throws RepositoryException
  {
    super(loader, errors);
    this.tgt = tgt;
    this.subRepositories = new ArrayList<>(subRepositories);
    setupRepositories(overwrite, loader);
  }

  public CompositeRepository(String classPath, CodeRepository tgt, boolean overwrite, ErrorReport errors)
      throws RepositoryException
  {
    this(classPath, tgt, overwrite, Thread.class.getClassLoader(), errors);
  }

  private void setupRepositories(boolean overwrite, ClassLoader loader) throws RepositoryException
  {
    generic = new RepositoryNodeImpl(new ResourceURI.NoUri(), "", new CompositeCodeCatalog(""));
    mergeGenerics(tgt.synthCodeCatalog());
    for (CodeRepository repo : subRepositories) {
      mergeGenerics(repo.synthCodeCatalog());
    }
    triggerUpdates(generic);
    for (RepositoryNode node : tgt) {
      cache.put(node.getUri(), node);
      triggerUpdates(node);
    }
    for (CodeRepository repo : subRepositories) {
      repo.addListener(this);
      for (RepositoryNode node : repo) {
        if (node != null) {
          cache.put(node.getUri(), node);
          triggerUpdates(node);
        }
      }
    }
    subRepositories.add(tgt);
    tgt.addListener(this);
  }

  /**
   * Add a repository to the composite repository.
   * 
   * @param repo
   * @throws RepositoryException
   */
  public void addRepository(CodeRepository repo) throws RepositoryException
  {
    mergeGenerics(repo.synthCodeCatalog());
    triggerUpdates(generic);
    repo.addListener(this);
    for (RepositoryNode node : repo) {
      if (node != null) {
        cache.put(node.getUri(), node);
        triggerUpdates(node);
      }
    }
  }

  private void setupRepositories(String classPath, boolean overwrite, ClassLoader loader) throws RepositoryException
  {
    String elements[] = classPath.split(File.pathSeparator);
    String wd = ApplicationProperties.getWd();
    String sep = File.separator;
    String driveSep = sep.equals("\\") ? ":" : null; // windows-specific code

    tgt.addListener(this);
    for (int ix = 0; ix < elements.length; ix++) {
      String seg = elements[ix];
      File path = (seg.startsWith("/") || (driveSep != null && seg.contains(driveSep)) ? new File(seg) : new File(wd,
          seg));
      if (path.isDirectory())
        subRepositories.add(new DirectoryRepository(path, overwrite, false, loader, errors));
      else if (path.canRead() && seg.endsWith(ZipArchive.EXTENSION))
        try {
          subRepositories.add(new ZipCodeRepository(path, loader, errors));
        } catch (IOException e) {
          errors.reportWarning("could not access archive file " + path);
        } catch (ResourceException e) {
          errors.reportWarning("resource problem in accessing archive file " + path);
        }
      else
        errors.reportWarning("ignoring repository at " + path);
    }
    setupRepositories(overwrite, loader);
  }

  private void mergeGenerics(CodeCatalog other) throws RepositoryException
  {
    CodeCatalog genCat = generic.getCode();
    genCat.mergeEntries(other);
  }

  @Override
  public RepositoryNode addRepositoryNode(ResourceURI uri, String hash, CodeCatalog code) throws RepositoryException
  {
    return tgt.addRepositoryNode(uri, hash, code);
  }

  @Override
  public void removeRepositoryNode(ResourceURI uri) throws CatalogException
  {
    tgt.removeRepositoryNode(uri);
    cache.remove(uri);
  }

  @Override
  public RepositoryNode findNode(ResourceURI uri)
  {
    return cache.get(uri);
  }

  @Override
  public CodeCatalog synthCodeCatalog()
  {
    return generic.getCode();
  }

  @Override
  public Iterator<RepositoryNode> iterator()
  {
    if (!subRepositories.isEmpty())
      return new ComboIterator<RepositoryNode>(subRepositories);
    else
      return new NullIterator<RepositoryNode>();
  }

  @Override
  public void nodeUpdated(RepositoryNode node)
  {
    cache.put(node.getUri(), node);
    triggerUpdates(node);
  }

  @Override
  public boolean removeNode(RepositoryNode node)
  {
    cache.remove(node.getUri());
    triggerDelete(node);
    return true;
  }
}
