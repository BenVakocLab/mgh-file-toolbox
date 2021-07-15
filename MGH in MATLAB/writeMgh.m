function writeMgh(operation, meta5, savefilename, varargin)
% works for three types of operation: clear, append, and write
% operation = 'clear', 'append', 'write'
% meta5 = [0 mghType nAlinesToProcTomo nZpixels nSlices]
% varargin = array2write : needed for operations 'append' and 'write'
%
% .mgh metadata information %%%%%%%
% metadata is 1024*1024 bytes (1048576 bytes):
% only first five integers are useful 
% 32-bit int (.mgh version, now set to 0)
% int .mghversion 0
% int datatype (0 = uint8, 1 = uint16, 2 = float32, 3 = float32 real/imag)   
%           (for type 3, usually tge next three numbers are not defined, 
%           i.e., no method for reading in data into imageJ)
% int width
% int height
% int n_slices
%
% %%% usage example : 'clear' and 'append' mode are used together to generate large volume
% writeMgh('clear', meta5, fullfile2save);
% for iFrame = 1:nFrames
%     iFrame
%     writeMgh('append',meta5, fullfile2save, array2save(:,:,iFrame));
% end
%
% %%% usage example2 : 'write' mode just writes the whole array. 
% dim2save = size(array2write); 
% mghType = 1 % save as uint16
% meta5 = [0, mghType, dim2save(2),dim2save(1), dim2save(3)];
% writeMgh('write', meta5, fullfile2save, array2save); 
%
% <<snam@mit.edu 20150930>>
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


if ( numel(meta5) ~= 5 )
    error('meta5 must be of length 5');
end

switch operation
    case 'clear'
        fileID = fopen(savefilename,'w+');
        metalen_bytes = 1048576; % 1024*1024 = 1024 kBytes
        metalength = metalen_bytes/4; % (int32 = 4bytes)
        metadata = zeros(metalength,1);
        metadata(1:5,1) = meta5(:);
        
        fseek(fileID,0,'bof');
        fwrite(fileID, metadata,'int32');
        fclose(fileID);
        
    case 'append'
        complexFlag = 0;
        fileID = fopen(savefilename,'a');
        if ( numel(varargin) == 0 )
            error('no input data to write');
        else
            array2write = varargin{1};
            
        end
        
        mghType = meta5(2);
        
        switch mghType
            case 0
                datatype = 'uint8';
            case 1
                datatype = 'uint16';
            case 2
                datatype = 'float32';
            case 3
                datatype = 'float32';
                complexFlag = 1;
            otherwise
                    error('error! readMGH: wrong file type');         
        end
        
        if complexFlag
                array2write = array2write(:); % make into a single column vector
                writeV = zeros(2*length(array2write),1);
                writeV(1:2:end)=real(array2write);
                writeV(2:2:end)=imag(array2write);
                
        else 
            % ImageJ and MATLAB reads binary files in different order
            temp = rot90(array2write,1);
            temp = flip(temp,1);
            writeV = temp(:);
        end
        
        fwrite(fileID, writeV, datatype);
        fclose(fileID);

        
        
    case 'write'
        fileID = fopen(savefilename,'w+');
        metalen_bytes = 1048576; % 1024*1024 = 1024 kBytes
        metalength = metalen_bytes/4; % (int32 = 4bytes)
        metadata = zeros(metalength,1);
        metadata(1:5,1) = meta5(:);
        
        fseek(fileID,0,'bof');
        fwrite(fileID, metadata,'int32');
        fclose(fileID);
         
        complexFlag = 0;
        fileID = fopen(savefilename,'a');
        if ( numel(varargin) == 0 )
            error('no input data to write');
        else
            array2write = varargin{1};
        end
        if ~isequal(size(array2write),[meta5(4),meta5(3),meta5(5)])
            warning('array size mismatch in the write operation: metadata updated');
            dim2save = size(array2write); 
            meta5(3:5) = [dim2save(2), dim2save(1), dim2save(3)];
            fileID = fopen(savefilename,'w+');
            metalen_bytes = 1048576; % 1024*1024 = 1024 kBytes
            metalength = metalen_bytes/4; % (int32 = 4bytes)
            metadata = zeros(metalength,1);
            metadata(1:5,1) = meta5(:);

            fseek(fileID,0,'bof');
            fwrite(fileID, metadata,'int32');
            fclose(fileID);

            complexFlag = 0;
            fileID = fopen(savefilename,'a');
        end
        
        mghType = meta5(2);
        
        switch mghType
            case 0
                datatype = 'uint8';
            case 1
                datatype = 'uint16';
            case 2
                datatype = 'float32';
            case 3
                datatype = 'float32';
                complexFlag = 1;
            otherwise
                    error('error! readMGH: wrong file type');         
        end
        
        if complexFlag
                array2write = array2write(:); % make into a single column vector
                writeV = zeros(2*length(array2write),1);
                writeV(1:2:end)=real(array2write);
                writeV(2:2:end)=imag(array2write);
                
        else 
            % ImageJ and MATLAB reads binary files in different order
            temp = rot90(array2write,1);
            temp = flip(temp,1); 
            % if this causes error due to the matlab version, use temp = filpud(temp);
            writeV = temp(:);
        end
        fwrite(fileID, writeV, datatype);
        fclose(fileID);

        
    otherwise
        warning('unexpected operation for writeMgh');
end
    
end

% .mgh metadata information %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% metadata is 1024*1024 bytes (1048576 bytes):
% only first five integers are useful 
% 32-bit int (.mgh version, now set to 0)
% int .mghversion 0
% int type (0 = uint8, 1 = uint16, 2 = float32, 3 = float32 real/imag)   
%           (for type 3, next three numbers are not defined, 
%           i.e., no method for reading in data into imageJ)
% int width
% int height
% int n_slices
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%