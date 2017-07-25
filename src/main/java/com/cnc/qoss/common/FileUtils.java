package com.cnc.qoss.common;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.zip.GZIPInputStream;

/**
 * @author Administrator
 * @Email yuxf@chinanetcenter.com
 * @Date 2014/6/11 10:33
 */
public class FileUtils {
	private static Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	public static FileLock tryLock(File lockF) throws IOException {
		lockF.deleteOnExit(); // 指定在退出时释放锁
		RandomAccessFile file = new RandomAccessFile(lockF, "rws"); // 指定要锁的文件
		FileLock res = null;
		try {
			res = file.getChannel().tryLock(); // 试图取得文件的锁
		} catch (OverlappingFileLockException oe) { // 文件被其它线程锁时抛出此异常
			logger.error("Cannot create lock on " + lockF, oe);
		} catch (IOException e) {
			logger.error("Cannot create lock on " + lockF, e);
		} finally{
			file.close();
		}
		return res;
	}
	
	/**
	 * 获取目录下一定个数的文件，并转移到目标目录
	 *
	 * @param srcPath 原目录
	 * @param desPath 目标目录
	 * @param fileNum 文件个数
	 * @return
	 */
	public static List<File> listFiles(String srcPath,String desPath, int fileNum,FilenameFilter filenameFilter,BlockingQueue<File> fileQueue) {
		List<File> files = new ArrayList<File>();
		File srcFilePath = new File(srcPath);
		String names[] = srcFilePath.list();
		if (names != null) {
			logger.info("path [" + srcPath + "] file num:" + names.length);
			for (String name : names) {
				Path sourcePath = FileSystems.getDefault().getPath(srcPath+"/"+name);
				Path targetPath = FileSystems.getDefault().getPath(desPath+"/"+name);
				if (".sdn-tmp".equals(name)) {
					continue;
				}
				if (name.endsWith(".temp")) {
					continue;
				}
				//如果被过滤就移动到备份文件夹
				if (filenameFilter!= null && !filenameFilter.accept(srcFilePath,name)) {
					try{                    
						Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
						continue;
					} catch (IOException e) {
						logger.error("move file error",e);
					}
				}
				if (files.size() >= fileNum) {
					return files;
				}
				File srcFile = new File(srcPath, name);
				if (srcFile.isDirectory()) {  //目录
					File desFile= new File(desPath, name);
					List<File> subFiles = listFiles(srcFile.getAbsolutePath(),desFile.getAbsolutePath(), fileNum - files.size(),filenameFilter,fileQueue);
					files.addAll(subFiles);
					deleteEmptyDir(srcFile);
				} else {   //单个文件
					files.add(srcFile);
					try {
						fileQueue.put(srcFile);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return files;
	}

	public static Path move(String srcPath,String destPath) throws IOException {
		Path sourcePath = FileSystems.getDefault().getPath(srcPath);
		Path destinationPath = FileSystems.getDefault().getPath(destPath);
		return Files.move(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);
	}
	/**
	 * 删除空目录
	 *
	 * @param srcFile 目录
	 */
	private static void deleteEmptyDir(File srcFile) {
		boolean delete = srcFile.delete();
		if (!delete) {
			logger.info("can not delete dir[" + srcFile.getAbsolutePath() + "]");
		}
	}

	/**
	 * 获取目录下所有的文件名称，递归获取子目录文件
	 *
	 * @param srcPath 目标目录
	 * @return 文件名称列表
	 */
	public static List<String> list(String srcPath) {
		List<String> result = new ArrayList<String>();

		File srcFilePath = new File(srcPath);
		String names[] = srcFilePath.list();
		for (String name : names) {
			File srcFile = new File(srcPath, name);
			if (srcFile.isDirectory()) {
				result.addAll(list(srcFile.getAbsolutePath()));
			} else {
				result.add(srcFile.getAbsolutePath());
			}
		}
		return result;
	}
	public static File uncompressGz(File file,String dst)  {
		InputStream is = null;
		InputStream gis = null;
		FileOutputStream fos=null;
		byte[] buffer = new byte[10*1024*1024];
		File unZip=new File(dst,file.getName().substring(0, file.getName().length() - 3));
		try {
			fos = new FileOutputStream(unZip);
			is = new FileInputStream(file);
			gis = new GZIPInputStream(is);
			int n;
			while ((n = gis.read(buffer))>= 0) {
				fos.write(buffer, 0, n);
			}
			fos.flush();
		} catch (Exception ex) {
			logger.error("Analysis file[" + file.getName() + "] failed." + ex.getMessage(), ex);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(gis);
			IOUtils.closeQuietly(fos);
		}
		return unZip;
	}
	public static List<String> readLinesGz(File file) {
		List<String> result = new ArrayList<String>();
		InputStream is = null;
		InputStream bis = null;
		Reader reader = null;
		BufferedReader in = null;
		try {
			is = new FileInputStream(file);
			bis = new GZIPInputStream(is);
			reader = new InputStreamReader(bis, Charset.defaultCharset());
			in = new BufferedReader(reader, 1 * 1024 * 1024);// 1M缓存
			while (in.ready()) {
				String line = in.readLine();
				result.add(line);
			}
			in.close();
			reader.close();
			bis.close();
			is.close();
		} catch (Exception ex) {
			logger.error("Analysis file[" + file.getName() + "] failed." + ex.getMessage(), ex);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(bis);
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(in);
		}
		return result;
	}
}
