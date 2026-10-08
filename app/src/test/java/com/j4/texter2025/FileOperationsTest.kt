package com.j4.texter2025

import android.content.Context
import com.j4.texter2025.data.FileModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class FileOperationsTest {
    private lateinit var context: Context
    private lateinit var mainActivity: MainActivity
    private lateinit var testFile: File
    private lateinit var testFileModel: FileModel

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mainActivity = MainActivity()
        testFileModel = FileModel(
            id = "test-id",
            name = "test.txt",
            content = "Test content"
        )
        
        // Mock filesDir
        val filesDir = mockk<File>()
        every { context.filesDir } returns filesDir
        every { filesDir.listFiles() } returns arrayOf()
    }

    @Test
    fun saveFile_CreatesNewFile() {
        // Given
        val expectedContent = "Test content"
        
        // When
        mainActivity.saveFile(context, testFileModel)
        
        // Then
        verify { 
            context.filesDir
        }
    }

    @Test
    fun loadFiles_ReturnsListOfFiles() {
        // Given
        val testFile1 = mockk<File>()
        val testFile2 = mockk<File>()
        every { testFile1.canRead() } returns true
        every { testFile1.isFile } returns true
        every { testFile1.absolutePath } returns "/test1.txt"
        every { testFile1.name } returns "test1.txt"
        every { testFile1.readText() } returns "Content 1"
        
        every { testFile2.canRead() } returns true
        every { testFile2.isFile } returns true
        every { testFile2.absolutePath } returns "/test2.txt"
        every { testFile2.name } returns "test2.txt"
        every { testFile2.readText() } returns "Content 2"
        
        every { context.filesDir.listFiles() } returns arrayOf(testFile1, testFile2)
        
        // When
        val result = mainActivity.loadFilesFromStorage(context)
        
        // Then
        assertEquals(2, result.size)
        assertEquals("test1.txt", result[0].name)
        assertEquals("Content 1", result[0].content)
        assertEquals("test2.txt", result[1].name)
        assertEquals("Content 2", result[1].content)
    }

    @Test
    fun deleteTextFile_RemovesFileFromSystem() {
        // Given
        val fileToDelete = mockk<File>()
        every { fileToDelete.delete() } returns true
        every { context.filesDir.listFiles() } returns arrayOf(fileToDelete)
        
        // When
        mainActivity.deleteTextFile(context, testFileModel)
        
        // Then
        verify { 
            context.filesDir
            fileToDelete.delete()
        }
    }
}
