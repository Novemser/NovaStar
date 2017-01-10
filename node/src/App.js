import React from 'react';
import './App.css';
import {Card, CardActions} from 'material-ui/Card';
import {Table, TableBody, TableFooter, TableHeader, TableHeaderColumn, TableRow, TableRowColumn}
    from 'material-ui/Table';
import LinearProgress from 'material-ui/LinearProgress';
import ActionBackup from 'material-ui/svg-icons/action/backup';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import ActionBuild from 'material-ui/svg-icons/action/build';
import ActionRestore from 'material-ui/svg-icons/action/restore';
import ActionZoomIn from 'material-ui/svg-icons/action/zoom-in';
import NavigationRefresh from 'material-ui/svg-icons/navigation/refresh';
import FileFolderOpen from 'material-ui/svg-icons/file/folder-open';
import FileFileDownload from 'material-ui/svg-icons/file/file-download';
import {red500, greenA200, yellow700} from 'material-ui/styles/colors';
import FileCloudCircle from 'material-ui/svg-icons/file/cloud-circle';
import CircularProgress from 'material-ui/CircularProgress';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import IconButton from 'material-ui/IconButton';
import FlatButton from 'material-ui/FlatButton';
import RaisedButton from 'material-ui/RaisedButton';
import TextField from 'material-ui/TextField';
import Subheader from 'material-ui/Subheader';
import Dialog from 'material-ui/Dialog';
import FileBlocksDetail from './FileBlocksDetail';
import 'jquery-form';
import $ from 'jquery';

const styles = {
    card : {
        margin : '0 auto',
        maxWidth : '1076px',
        padding : '32px 0',
    },
    table : {
        overflowX : 'scroll',
        overflowY : 'hidden',
        background : 'transparent',
    },
    tableBody : {
        overflowX : 'auto',
        overflowY : 'hidden',
    },
    tableRightColumn : {
        textAlign : 'right',
    },
    button: {
        margin: 12,
    },
    fileInput: {
        cursor: 'pointer',
        position: 'absolute',
        top: 0,
        bottom: 0,
        right: 0,
        left: 0,
        width: '100%',
        opacity: 0,
    },
    circularProgress : {
        textAlign : 'center',
    },
    iconStyles : {
        marginLeft : 8,
        marginRight: 24,
        verticalAlign : 'bottom',
    },
    slaveStatus : {
        display : 'inline-block',
        textAlign : 'center',

    },

};

const URL = '/api';

const API = {
    Upload : URL + '/upload',
    Refresh : URL + '/refresh',
    Download : URL + '/download',
    Delete : URL + '/delete',
    Format : URL + '/format',
    Rename : URL + '/rename',
    NodeStatus : URL + '/node',
    BlockSize : URL + '/blocksize',
    SpaceStatus : URL + '/space',
};

class FormatDialog extends React.Component {
    state = {
        open: false,
    };

    handleOpen = () => {
        this.setState({
            open: true,
            progress : false
        });
    };

    handleClose = () => {
        this.setState({open: false});
    };

    handleSubmit = () => {
        $.ajax({
            url : API.Format,
            type : 'GET',
            contentType: 'application/json;charset=UTF-8',
            success : function(data, textStatus, jqXHR) {
                //console.log(data);
                this.props.onSuccess();
                this.setState({
                    open: false,
                    progress : false
                });
            }.bind(this),
            error : function(xhr, textStatus) {
                console.log(xhr.status + '\n' + textStatus + '\n');
                this.setState({open: false});
            }.bind(this)
        });
        this.setState({
            progress : true,
        });
    };

    render() {
        const actions = [
            <FlatButton
                label="Cancel"
                primary={true}
                style={{display : this.state.progress ? 'none' : 'inline-block'}}
                onTouchTap={this.handleClose}
            />,
            <FlatButton
                label="Yes"
                primary={true}
                style={{display : this.state.progress ? 'none' : 'inline-block'}}
                onTouchTap={this.handleSubmit}
            />,
        ];

        return (
            <div>
                <Dialog
                    actions={actions}
                    modal={false}
                    open={this.state.open}
                    onRequestClose={()=>{if(!this.state.progress) this.handleClose()}}
                >
                    {this.state.progress ? <div style={styles.circularProgress} ><CircularProgress /></div> : 'Format The File System?'}
                </Dialog>
            </div>
        );
    }
}

class DeleteFileDialog extends React.Component {
    state = {
        open: false,
        fileId : null,
    };

    handleOpen = (fileId) => {
        this.setState({
            open: true,
            fileId : fileId,
            progress : false,
        });
    };

    handleClose = () => {
        this.setState({open: false});
    };

    handleSubmit = () => {
        const data = {id : this.state.fileId};
        $.ajax({
            url : API.Delete,
            type : 'POST',
            data : JSON.stringify(data),
            contentType: 'application/json;charset=UTF-8',
            success : function(data, textStatus, jqXHR) {
                //console.log(data);
                this.setState({open: false});
                this.props.onSuccess();
            }.bind(this),
            error : function(xhr, textStatus) {
                console.log(xhr.status + '\n' + textStatus + '\n');
                this.setState({open: false});
            }.bind(this)
        });
        this.setState({
            progress : true,
        });
    };

    render() {
        const actions = [
            <FlatButton
                label="Cancel"
                primary={true}
                style={{display : this.state.progress ? 'none' : 'inline-block'}}
                onTouchTap={this.handleClose}
            />,
            <FlatButton
                label="Submit"
                primary={true}
                style={{display : this.state.progress ? 'none' : 'inline-block'}}
                onTouchTap={this.handleSubmit}
            />,
        ];

        return (
            <div>
                <Dialog
                    actions={actions}
                    modal={false}
                    open={this.state.open}
                    onRequestClose={()=>{if(!this.state.progress) this.handleClose()}}
                >
                    {this.state.progress ? <div style={styles.circularProgress} ><CircularProgress /></div> : 'Delete This File?'}
                </Dialog>
            </div>
        );
    }
}

class RenameFileDialog extends React.Component {
    state = {
        open: false,
        fileId : null,
        filename : null,
    };

    handleOpen = (fileId, fileName) => {
        this.setState({
            open: true,
            fileId : fileId,
            filename : fileName
        });
    };

    handleClose = () => {
        this.setState({open: false});
    };

    handleChange = (event) => {
        this.setState({
            filename: event.target.value,
        });
    };

    handleSubmit = () => {
        const data = {id : this.state.fileId, name : this.state.filename};
        $.ajax({
            url : API.Rename,
            type : 'POST',
            data : JSON.stringify(data),
            contentType: 'application/json;charset=UTF-8',
            success : function(data, textStatus, jqXHR) {
                //console.log(data);
                this.setState({open: false});
                this.props.onSuccess();
            }.bind(this),
            error : function(xhr, textStatus) {
                console.log(xhr.status + '\n' + textStatus + '\n');
            }
        });


    };

    render() {
        const actions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onTouchTap={this.handleClose}
            />,
            <FlatButton
                label="Submit"
                primary={true}
                onTouchTap={this.handleSubmit}
            />,
        ];

        return (
            <div>
                <Dialog
                    title="Rename File"
                    actions={actions}
                    modal={false}
                    open={this.state.open}
                    onRequestClose={this.handleClose}
                >

                    <TextField
                        onChange={this.handleChange}
                        value={this.state.filename}
                        hintText="New File Name"
                        floatingLabelText="File Name"
                    /><br />
                </Dialog>
            </div>
        );
    }
}

class UploadFileDialog extends React.Component {
    state = {
        open: false,
    };

    handleOpen = () => {
        this.setState({open: true});
    };

    handleClose = () => {
        this.setState({open: false});
    };

    handleSubmit = () => {
        var form = $('#uploadFileForm').ajaxSubmit();
        var xhr = form.data('jqxhr');

        xhr.done(function(data) {
            console.log(data);
            if(!data) alert('Duplicated File Name');
            this.props.onSuccess();
        }.bind(this));
        //var submit = $('#submitBtn');
        //submit.click();
        this.setState({open: false});
        this.props.onUpload();
    };

    render() {
        const actions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onTouchTap={this.handleClose}
            />,
            <FlatButton
                label="Submit"
                primary={true}
                onTouchTap={this.handleSubmit}
            />,
        ];

        return (
            <div>
                <Dialog
                    title="Upload File"
                    actions={actions}
                    modal={false}
                    open={this.state.open}
                    onRequestClose={this.handleClose}
                >
                    <form id="uploadFileForm" encType="multipart/form-data" method="post" action={API.Upload} target="uploadFrame">
                        <RaisedButton
                            label="Choose A File"
                            style={styles.button}
                            containerElement="label"
                            primary={true}
                            icon={<FileFolderOpen/>}
                        >
                            <input type="file" style={styles.fileInput} name="file"/>
                        </RaisedButton>
                        <input type="submit" id="submitBtn" style={{display:'none'}} />
                    </form>
                </Dialog>
            </div>
        );
    }
}


class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            disablePreviousButton : true,
            disableNextButton : true,

            pages_num : 1,
            current_page : 1,
            current_list : [],

            file_list : [],
            progress : false,
            blockSize : 32,

            slaves : [null, null, null, null, null],
            used_space : 0,
            capacity : 0,
        };
    }

    getNodeStatus = () => {
        $.ajax({
            url : API.NodeStatus,
            type : 'GET',
            contentType: 'application/json;charset=UTF-8',
            success : function(data, textStatus, jqXHR) {
                //console.log(data);
                this.setState({slaves : data});
            }.bind(this),
            error : function(xhr, textStatus) {
                this.setState({slaves : [null, null, null, null, null]});
                console.log(xhr.status + '\n' + textStatus + '\n');
            }.bind(this)
        });
        setTimeout(this.getNodeStatus, 16000);
    };

    getSpaceStatus = () => {
        $.ajax({
            url : API.SpaceStatus,
            type : 'GET',
            contentType: 'application/json;charset=UTF-8',
            success : function(data, textStatus, jqXHR) {
                //console.log(data);
                this.setState({
                    capacity : data.total,
                    used_space : data.used,
                });
            }.bind(this),
            error : function(xhr, textStatus) {
                console.log(xhr.status + '\n' + textStatus + '\n');
            }
        });
    };

    formatFileSystem = () => {
        this.refs.FormatDialog.handleOpen();
    };

    getFileList = () => {
        $.ajax({
            url : API.Refresh,
            type : 'GET',
            contentType: 'application/json;charset=UTF-8',
            success : function(data, textStatus, jqXHR) {
                console.log(data);

                const list = data.file_list;

                let pages_num = parseInt((list.length / 10), 10) + ((list.length % 10 > 0) ? 1 : 0) || 1;
                let current_page = Math.min(this.state.current_page, pages_num);
                let current_list = list.slice((current_page - 1) * 10, (current_page - 1) * 10 + 10);
                this.setState({
                    current_page : current_page,
                    blockSize : data.BLOCK_SIZE,
                    file_list : list,
                    pages_num : pages_num,
                    current_list : current_list,
                    disableNextButton: ((parseInt(current_page, 10)) === pages_num),
                    disablePreviousButton: ((parseInt(current_page, 10)) === 1),
                });
                this.getSpaceStatus();
            }.bind(this),
            error : function(xhr, textStatus) {
                console.log(xhr.status + '\n' + textStatus + '\n');
            }
        });
    };

    openUploadDialog = () => {
        this.refs.Dialog.handleOpen();
    };

    showProgress = () => {
        this.setState({
            progress : true
        })
    };

    hiddenProgress = () => {
        this.setState({
            progress : false
        });
        this.getFileList();
        setTimeout(this.getFileList, 1000);
    };

    handleChange = (event, index, value) => {
        $.ajax({
            url : API.BlockSize,
            type : 'GET',
            data : {
                size : value,
            },
            contentType: 'application/json;charset=UTF-8',
            success : function(data, textStatus, jqXHR) {
                this.setState({blockSize : value});
            }.bind(this),
            error : function(xhr, textStatus) {
                console.log(xhr.status + '\n' + textStatus + '\n');
            }
        });
    };

    renameFile = (index) => {
        const file = this.state.current_list[index];
        this.refs.RenameDialog.handleOpen(file.id, file.name);
    };

    downloadFile = (index) => {
        const file = this.state.current_list[index];
        console.log(file.id);
        this.showProgress();
        $.ajax({
            url : API.Download,
            type : 'GET',
            data : {
                id : file.id,
            },
            contentType: 'application/json;charset=UTF-8',
            success : function(data, textStatus, jqXHR) {
                console.log(data);
                this.hiddenProgress(true);
                if(data !== 'File Not Found') {
                    var time = 500;
                    if(data.slice(-3) === 'mp4') time = 5000;
                    setTimeout(()=>window.open(data), time);
                }
            }.bind(this),
            error : function(xhr, textStatus) {
                console.log(xhr.status + '\n' + textStatus + '\n');
            }
        });
    };

    removeFile = (index) => {
        const file = this.state.current_list[index];
        console.log(file.id);
        this.refs.DeleteDialog.handleOpen(file.id);
    };

    componentDidMount() {
        this.getFileList();
        setInterval(this.getFileList, 32000);
        this.getNodeStatus();
        this.handleChange(null, null, this.state.blockSize);
    }

    previousPage= () => {
        let current_page = parseInt(this.state.current_page, 10) - 1;
        let current_list = this.state.file_list.slice((current_page - 1) * 10, (current_page - 1) * 10 + 10);
        this.setState({
            current_page : current_page,
            current_list : current_list,
            disableNextButton: false,
            disablePreviousButton: ((parseInt(this.state.current_page, 10) - 1) === 1),
        });
    };

    nextPage = () => {
        let current_page = (parseInt(this.state.current_page, 10) + 1);
        let current_list = this.state.file_list.slice((current_page - 1) * 10, (current_page - 1) * 10 + 10);
        this.setState({
            current_page : current_page,
            current_list : current_list,
            disableNextButton: ((parseInt(this.state.current_page, 10) + 1) === parseInt(this.state.pages_num, 10)),
            disablePreviousButton: false,
        });
    };

    render() {
        return (
            <div>
              {/*<div className="title">*/}
                  {/*<h1>NovaStar</h1>*/}
                  {/*<h2>A Tiny Flat Distributed File System</h2>*/}
              {/*</div>*/}
                {this.state.progress ? <LinearProgress mode="indeterminate" color="#2EFF99" /> : null}
                <Card>
                  <div style={styles.card}>
                      <div style={{textAlign : 'center'}}>
                          <Subheader>Node Status</Subheader>
                          {this.state.slaves.map((slave, index)=>(
                              <div key={index} style={styles.slaveStatus}>Node-{index + 1}
                                <FileCloudCircle style={styles.iconStyles} color={slave === null ? yellow700 : (slave ? greenA200 : red500)} />
                              </div>
                          ))}
                          <br />
                          <SelectField
                              style={{textAlign : 'left', marginTop : 16}}
                              floatingLabelText="File Block Size"
                              value={this.state.blockSize}
                              onChange={this.handleChange}
                          >
                              <MenuItem value={2} primaryText="2M" />
                              <MenuItem value={8} primaryText="8M" />
                              <MenuItem value={16} primaryText="16M" />
                              <MenuItem value={32} primaryText="32M" />
                              <MenuItem value={64} primaryText="64M" />
                              <MenuItem value={128} primaryText="128M" />
                              <MenuItem value={256} primaryText="256M" />
                              <MenuItem value={512} primaryText="512M" />
                          </SelectField>
                          <br />
                      </div>



                      <Table
                          style={styles.table}
                          bodyStyle={styles.tableBody}
                          selectable={false}>
                          <TableHeader
                              displaySelectAll={false}
                              adjustForCheckbox={false}>
                              <TableRow>
                                  <TableHeaderColumn colSpan="5" style={{fontSize : '2vh', textAlign : 'center'}}>
                                      File List
                                  </TableHeaderColumn>
                              </TableRow>
                              <TableRow>
                                  <TableHeaderColumn colSpan="2" style={{fontSize : '2vh'}}>File Name</TableHeaderColumn>
                                  <TableHeaderColumn style={{fontSize : '2vh'}}>File Size</TableHeaderColumn>
                                  <TableHeaderColumn style={{fontSize : '2vh'}}>Uploaded Time</TableHeaderColumn>
                                  <TableHeaderColumn colSpan="1" style={{fontSize : '2vh', textAlign : 'right'}}>Operation</TableHeaderColumn>
                              </TableRow>
                          </TableHeader>
                          <TableBody
                              showRowHover={true}
                              displayRowCheckbox={false}>
                              {this.state.current_list.map((file, index) => (
                                  <TableRow key={index}>
                                      <TableRowColumn colSpan="2">{file.name}</TableRowColumn>
                                      <TableRowColumn>{file.size}</TableRowColumn>
                                      <TableRowColumn>{file.uploadTime}</TableRowColumn>
                                      <TableRowColumn colSpan="1" style={styles.tableRightColumn} id={index}>
                                          <IconButton
                                              iconStyle={{color : 'rgb(0, 188, 212)'}}
                                              tooltip="Detail"
                                              tooltipPosition="center-center"
                                              onTouchTap={()=>{this.refs.DetailDialog.handleOpen(this.state.current_list[index])}}>
                                              <ActionZoomIn/>
                                          </IconButton>
                                          <IconButton
                                              iconStyle={{color : 'rgb(0, 188, 212)'}}
                                              tooltip="Download"
                                              tooltipPosition="center-center"
                                              onTouchTap={()=>{this.downloadFile(index)}}>
                                              <FileFileDownload/>
                                          </IconButton>
                                          <IconButton
                                              iconStyle={{color : 'rgb(0, 188, 212)'}}
                                              tooltip="Rename"
                                              tooltipPosition="center-center"
                                              onTouchTap={()=>{this.renameFile(index)}}>
                                              <ActionBuild/>
                                          </IconButton>
                                          <IconButton
                                              iconStyle={{color : 'rgb(0, 188, 212)'}}
                                              tooltip="Delete"
                                              tooltipPosition="center-center"
                                              onTouchTap={()=>{this.removeFile(index)}}>
                                              <ActionDelete/>
                                          </IconButton>
                                      </TableRowColumn>
                                  </TableRow>
                              ))}
                          </TableBody>
                          <TableFooter
                              adjustForCheckbox={false}>
                          </TableFooter>
                      </Table>

                      <CardActions>
                          <RaisedButton
                              label="Upload"
                              style={styles.button}
                              containerElement="label"
                              primary={true}
                              onTouchTap={this.openUploadDialog}
                              icon={<ActionBackup/>}/>
                          <RaisedButton
                              label="Refresh"
                              style={styles.button}
                              containerElement="label"
                              primary={true}
                              onTouchTap={this.getFileList}
                              icon={<NavigationRefresh/>}/>
                          <RaisedButton
                              label="Format"
                              style={styles.button}
                              containerElement="label"
                              primary={true}
                              onTouchTap={this.formatFileSystem}
                              icon={<ActionRestore/>}/>
                          <div style={{float:'right', marginTop:12}}>
                            <FlatButton  primary={true} label="PREVIOUS" onTouchTap={this.previousPage} disabled={this.state.disablePreviousButton} />
                            {this.state.current_page}/{this.state.pages_num || 1}
                            <FlatButton style={{textAlign : 'right', minWidth : 0}}  primary={true} label="NEXT" onTouchTap={this.nextPage} disabled={this.state.disableNextButton} />
                          </div>
                          <div style={{clear:'both'}}/>
                      </CardActions>
                      <div style={{textAlign : 'right', color : 'rgba(0, 0, 0, 0.6)', margin: '0px 32px 16px 0px'}}>
                          {'Used-Space : ' + this.state.used_space + ' | Capacity : ' +　this.state.capacity}
                      </div>
                  </div>
                </Card>
                <UploadFileDialog ref="Dialog" onUpload={this.showProgress} onSuccess={this.hiddenProgress}/>
                <RenameFileDialog ref="RenameDialog" onSuccess={this.hiddenProgress}/>
                <DeleteFileDialog ref="DeleteDialog" onSuccess={this.hiddenProgress}/>
                <FormatDialog ref="FormatDialog" onSuccess={this.hiddenProgress}/>
                <FileBlocksDetail ref="DetailDialog"/>
            </div>
        );
    }
}

export default App;
