import React from 'react';
import { Layout } from 'antd';
import './zzFooter.less';

const { Footer } = Layout;

class ZZFooter extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
        <Footer style={{ textAlign: 'center',backgroundColor:'transparent' }}>
            <div>{"Copyright © 2012 - "+new Date().getFullYear()+" Hoolark Inc. All Rights Reserved"}</div>
        </Footer>
    );
  }
}

export default ZZFooter;
