/**
 * Created by tsengkasing on 1/6/2017.
 */
import React from 'react';
import {Card} from 'material-ui/Card';
import {Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn}
    from 'material-ui/Table';
import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';
import Subheader from 'material-ui/Subheader';
import {lightBlue500} from 'material-ui/styles/colors';

const styles = {
    card : {
        margin : '16px auto',
        maxWidth : '1024px',
        padding : '4px 0',
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
        paddingRight : 0,
    },
    button: {
        margin: 12,
    },
    dialogWidth : {
        width : '100%',
        maxWidth : '896px',
    },
};



export default class FileBlocksDetail extends React.Component {
    state = {
        open: false,
        file : {
            realBlockCount : 0,
            fileBlocks : [],
        },
    };

    parseFileBlocks = (fileBlocks) => {
        var blocks_all = [];
        for (let i = 0; i < this.state.file.realBlockCount; i++) {
            var redundant_blocks = [];
            for (var block in fileBlocks) {
                if (fileBlocks[block].index === (i + 1)) {
                    redundant_blocks.push(fileBlocks[block].block);
                }
            }
            blocks_all.push(redundant_blocks);
        }
        return blocks_all;
    };

    handleOpen = (file) => {
        this.setState({
            open: true,
            file : file,
        });
        console.log(file);
    };

    handleClose = () => {
        this.setState({open: false});
    };

    render() {
        const actions = [
            <FlatButton
                label="Close"
                primary={true}
                keyboardFocused={false}
                onTouchTap={this.handleClose}
            />,
        ];


        return (
            <div>
                <Dialog
                    title="File Detail"
                    actions={actions}
                    modal={false}
                    contentStyle={styles.dialogWidth}
                    open={this.state.open}
                    onRequestClose={this.handleClose}
                    autoScrollBodyContent={true}
                >
                    <div style={{textAlign : 'center'}}>
                        <Subheader>Total Blocks</Subheader>
                        <h2 style={{color : lightBlue500, margin : 0}} >{this.state.file.realBlockCount}</h2>

                    </div>
                    {(this.parseFileBlocks(this.state.file.fileBlocks)).map((block, index)=>(
                        <Card key={index}>
                            <div style={styles.card}>
                                <Table
                                    style={styles.table}
                                    bodyStyle={styles.tableBody}
                                    selectable={false}>
                                    <TableHeader
                                        displaySelectAll={false}
                                        adjustForCheckbox={false}>
                                        <TableRow>
                                            <TableHeaderColumn colSpan="5" style={{fontSize : '2vh', textAlign : 'center'}}>
                                                Block-{index + 1}
                                            </TableHeaderColumn>
                                        </TableRow>
                                        <TableRow>
                                            <TableHeaderColumn colSpan="2" style={{fontSize : '1.5vh'}}>Block Name</TableHeaderColumn>
                                            <TableHeaderColumn style={{fontSize : '1.5vh', textAlign : 'center'}}>Block Size</TableHeaderColumn>
                                            <TableHeaderColumn style={{fontSize : '1.5vh', textAlign : 'center'}}>Node Name</TableHeaderColumn>
                                            <TableHeaderColumn style={{fontSize : '1.5vh', textAlign : 'right'}}>Node Address</TableHeaderColumn>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody
                                        showRowHover={true}
                                        displayRowCheckbox={false}>
                                        {block.map((_block, _index) => (
                                            <TableRow key={_index}>
                                                <TableRowColumn colSpan="2">{_block.blockName}</TableRowColumn>
                                                <TableRowColumn style={{textAlign : 'center'}}>{_block.blockSize}M</TableRowColumn>
                                                <TableRowColumn style={{textAlign : 'center'}}>{_block.slaveName}</TableRowColumn>
                                                <TableRowColumn style={{textAlign : 'right'}}>{_block.slaveIP}</TableRowColumn>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </div>
                        </Card>
                    ))}

                </Dialog>
            </div>
        );
    }
}