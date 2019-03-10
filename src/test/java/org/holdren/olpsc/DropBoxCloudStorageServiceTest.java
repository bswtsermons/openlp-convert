package org.holdren.olpsc;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import org.holdren.olpsc.cs.CloudStorageException;
import org.holdren.olpsc.cs.DropBoxCloudStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class DropBoxCloudStorageServiceTest
{
    @InjectMocks
    private DropBoxCloudStorageService dropBoxCloudStorage;

    @Mock
    private DbxClientV2 dbxClient;

    @Mock
    private DbxUserFilesRequests dbxUserFilesRequests;

    @Mock
    private UploadBuilder uploadBuilder;

    @Test
    public void testGoodStorage() throws IOException
    {
        String path = "/mock-path.txt";
        when(dbxClient.files()).thenReturn(dbxUserFilesRequests);
        when(dbxUserFilesRequests.uploadBuilder(path)).thenReturn(uploadBuilder);

        try (InputStream is = new ByteArrayInputStream(new byte[0]))
        {
            try
            {
                dropBoxCloudStorage.store(path, is);
            }
            catch (CloudStorageException cse)
            {
                fail("cloud storage exception thrown", cse);
            }

            // hate to put these in here but need to reference the byte array
            verify(dbxClient).files();
            verify(dbxUserFilesRequests).uploadBuilder(path);
            try
            {
                verify(uploadBuilder).uploadAndFinish(is);
            }
            catch (Exception e) { }

        }
    }

    @Test
    public void testUploadErrorExceptionTranslated() throws IOException
    {
        String path = "/mock-path.txt";
        when(dbxClient.files()).thenReturn(dbxUserFilesRequests);
        when(dbxUserFilesRequests.uploadBuilder(path)).thenReturn(uploadBuilder);

        UploadErrorException uploadErrorException = new UploadErrorException("routeName", "requestId", null, UploadError.OTHER);

        try
        {
            when(uploadBuilder.uploadAndFinish(any(ByteArrayInputStream.class))).thenThrow(uploadErrorException);
        } catch (DbxException e) {}

        try (InputStream is = new ByteArrayInputStream(new byte[0]))
        {
            assertThatThrownBy(() -> {
                dropBoxCloudStorage.store(path, is);
            }).isInstanceOf(CloudStorageException.class).withFailMessage("error uploading data");

        }
    }

    @Test
    public void testIOExceptionTranslated() throws IOException
    {
        String path = "/mock-path.txt";
        when(dbxClient.files()).thenReturn(dbxUserFilesRequests);
        when(dbxUserFilesRequests.uploadBuilder(path)).thenReturn(uploadBuilder);

        //UploadErrorException uploadErrorException = new UploadErrorException("routeName", "requestId", null, UploadError.OTHER);
        IOException ioException = new IOException("bleh");

        try
        {
            when(uploadBuilder.uploadAndFinish(any(ByteArrayInputStream.class))).thenThrow(ioException);
        } catch (DbxException e) {}

        try (InputStream is = new ByteArrayInputStream(new byte[0]))
        {
            assertThatThrownBy(() -> {
                dropBoxCloudStorage.store(path, is);
            }).isInstanceOf(CloudStorageException.class).withFailMessage("error uploading data");

        }
    }



}
