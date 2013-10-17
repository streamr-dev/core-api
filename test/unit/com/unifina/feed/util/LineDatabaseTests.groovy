package com.unifina.feed.util

import grails.test.mixin.*
import grails.test.mixin.support.*

import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class LineDatabaseTests {

	Path file
	
    void setUp() {
		String filename = "test/unit/com/unifina/feed/util/LineDatabaseTests.txt"
//		String filename = "test/unit/com/unifina/feed/util/10-15-ITCH.txt"
		file = Paths.get(filename)
		if (!Files.exists(file))
			throw new FileNotFoundException(filename)
    }

    void tearDown() {
        // Tear down logic here
    }

	void testInit() {
		long time = System.currentTimeMillis()
		LineDatabase db = new LineDatabase(file,10)
		println "Loading with interval 10 took "+(System.currentTimeMillis()-time)+" millis"
		assert db.size() == 163548
		db.close()
		
		time = System.currentTimeMillis()
		db = new LineDatabase(file,1000)
		println "Loading with interval 1000 took "+(System.currentTimeMillis()-time)+" millis"
		assert db.size() == 163548
		db.close()
	}
	
    void testGetLines() {
		LineDatabase db = new LineDatabase(file,100)
		assert db.getLines(1,1)[0] == "T11019"
		assert db.getLines(50248,1)[0] == "A  2950921B      250   992   2724000"
		assert db.getLines(84799,1)[0] == "A  5402757S      303   992   2738000"
		
		def lines = db.getLines(22239,7)
		assert lines.size()==7
		assert lines[0] == "A  1338467S     1000   992   2731000"
		assert lines[1] == "D  1338432"
		assert lines[2] == "A  1338468S      250   992   2732000"
		assert lines[3] == "A  1338469S      400   992   2732000"
		assert lines[4] == "A  1338470S      400   992   2732000"
		assert lines[5] == "A  1338471S      400   992   2732000"
		assert lines[6] == "A  1338472S      400   992   2732000"
		
		assert db.getLines(db.size(),1).size()==1
		assert db.getLines(db.size(),100).size()==1
		assert db.getLines(db.size()+1,1).size()==0
		assert db.getLines(db.size()+1,100).size()==0
		assert db.getLines(db.size()-9,100).size()==10
		db.close()
    }
	
	void testWriteAndRead() {
		Path temp = Files.createTempFile("LineDatabaseTests", ".tmp");
		FileInputStream source = new FileInputStream(file.toFile()); 
		FileOutputStream destination = new FileOutputStream(temp.toFile());   
		FileChannel sourceFileChannel = source.getChannel(); 
		FileChannel destinationFileChannel = destination.getChannel();   
		long size = sourceFileChannel.size(); 
		sourceFileChannel.transferTo(0, size, destinationFileChannel);
		source.close()
		destination.close()
		
		LineDatabase db = new LineDatabase(temp,10)
		int oldMax = db.size()
		db.addLine("FOO FOO")
		assert db.size() == oldMax+1
		
		db.addLine("BAR BAR")
		def lines = db.getLines(oldMax,100)
		assert lines.size()==3
		assert lines[1]=="FOO FOO"
		assert lines[2]=="BAR BAR"
		
		db.close()
		Files.delete(temp)
	}

	void testGetLinePerformanceRandom() {
		LineDatabase db = new LineDatabase(file,10)
		long max = db.size()
		
		long time = System.currentTimeMillis()
		for (int i=0;i<10000;i++) {
			long line = (long) Math.random()*max
			db.getLines(line,1)
		}
		
		println "Random line performance with interval 10 took "+(System.currentTimeMillis()-time)+" millis"
		db.close()
		
		db = new LineDatabase(file,100)
		max = db.size()
		
		time = System.currentTimeMillis()
		for (int i=0;i<10000;i++) {
			long line = (long) Math.random()*max
			db.getLines(line,1)
		}
		
		println "Random line performance with interval 100 took "+(System.currentTimeMillis()-time)+" millis"
		db.close()
	}
	
	void testGetLinePerformanceSequential() {
		LineDatabase db = new LineDatabase(file,10)
		long max = db.size()
		
		long time = System.currentTimeMillis()
		db.getLines(123456,10000)
		
		println "Sequential line performance with interval 10 took "+(System.currentTimeMillis()-time)+" millis"
		db.close()
		
		db = new LineDatabase(file,100)
		max = db.size()
		
		time = System.currentTimeMillis()
		db.getLines(123456,10000)
		
		println "Sequential line performance with interval 100 took "+(System.currentTimeMillis()-time)+" millis"
		db.close()
	}
}
